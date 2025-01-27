package cantilevermodelmaker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.joml.Matrix4d;
import org.joml.Vector2d;
import org.joml.Vector3d;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cantilevermodelmaker.ObjGen.Model.Additional;

public class ObjGen {

    static final double PIXEL = 1d / 16d;
    static final double THICKNESS = 1;    
    static final double DIAMETER = PIXEL * THICKNESS;
    static final String NAMESPACE = "pantographsandwires";

    static final int MIN_WIDTH = 3;
    static final int MAX_WIDTH = 7;

    static record Pair<A, B>(A a, B b) {}
    static record BlockState(String model, int y) {}
    static class BlockStates {
        final Map<String, BlockState> variants = new HashMap<>();
    }

    static enum EFacing {
        NORTH("north", 0),
        SOUTH("south", 180),
        EAST("east", 90),
        WEST("west", 270);

        final String name;
        final int y;
        EFacing(String name, int y) {
            this.name = name;
            this.y = y;
        }
    }

    public static void main(String[] args) throws Exception {
        String input = "cantilever_cube.obj";

        double[] attachPixels = new double[] { 16, 12, 8, 5, 4 };
        String[] armTypes = new String[] { "center", "inner", "outer" };
        String[] insulatorTypes = new String[] { "green", "brown" };
        String[] insulatorPlacement = new String[] { "back", "front" };

        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

        // SINGLE
        for (int width = MIN_WIDTH; width <= MAX_WIDTH; width++) {
            for (double px : attachPixels) {
                for (String armType : armTypes) {
                    String outDir =  String.format("output/cantilever/single/%s", width, armType, (int)px);  
                    String outPath = Paths.get(outDir, String.format("cantilever_%s_%spx.obj", armType, (int)px)).toString();
                    new File(outDir).mkdirs();
                    String wireArmPath = String.format("contact_wire_holder_%s.obj", armType);

                    Obj obj = new Obj();
                    final int fPx = (int)px;

                    Map<Integer, Pair<InsulatorPlacement, InsulatorPlacement>> cantileverAttachments = new HashMap<>();
                    List<InsulatorPlacement> placements = makeCantilever(obj, input, wireArmPath, width + 0, PIXEL * (px / 2d), ((float)width + 1.25f) / 2f, 0, 0, 0,
                    (top, bottom) -> {
                        switch (fPx) {
                            case 8:
                            case 12:
                                cantileverAttachments.put(fPx, new Pair<>(
                                    new InsulatorPlacement(new float[] { (float)(top.x) * 16, (float)top.y * 16, (float)top.z * 16 }, new float[3]),
                                    new InsulatorPlacement(new float[] { (float)(bottom.x) * 16, (float)bottom.y * 16, (float)bottom.z * 16 }, new float[3])
                                ));
                                break;
                            default:
                                break;
                        }
                    });
                    obj.save(outPath);
                    
                    for (String insulatorType : insulatorTypes) {
                        for (String placement : insulatorPlacement) {
                            List<Additional> additional = new ArrayList<>();
                            if (placement.equals("back")) {
                                additional.addAll(List.of(
                                    new Additional(String.format(NAMESPACE + ":models/block/insulator/base/%s_insulator.obj", insulatorType), placements.get(0).rot, placements.get(0).pos, true),
                                    new Additional(String.format(NAMESPACE + ":models/block/insulator/base/%s_insulator.obj", insulatorType), placements.get(1).rot, placements.get(1).pos, true)
                                ));
                            } else if (placement.equals("front")) {
                                additional.addAll(List.of(
                                    new Additional(String.format(NAMESPACE + ":models/block/insulator/base/%s_insulator.obj", insulatorType), placements.get(2).rot, placements.get(2).pos, true),
                                    new Additional(String.format(NAMESPACE + ":models/block/insulator/base/%s_insulator.obj", insulatorType), placements.get(3).rot, placements.get(3).pos, true),
                                    new Additional(String.format(NAMESPACE + ":models/block/insulator/base/%s_insulator.obj", insulatorType), placements.get(4).rot, placements.get(4).pos, true)
                                ));
                            }
                            if (cantileverAttachments.containsKey(fPx)) {
                                additional.add(new Additional(String.format(NAMESPACE + ":models/block/cantilever/post_connection/%spx.obj", fPx), cantileverAttachments.get(fPx).a.rot, cantileverAttachments.get(fPx).a.pos, false));
                                additional.add(new Additional(String.format(NAMESPACE + ":models/block/cantilever/post_connection/%spx.obj", fPx), cantileverAttachments.get(fPx).b.rot, cantileverAttachments.get(fPx).b.pos, false));
                            }
                            Model model = new Model(String.format(NAMESPACE + ":models/block/cantilever/single/%s/cantilever_%s_%spx.obj", width, armType, (int)px), additional);
                            
                            String json = gson.toJson(model);
                            try (FileWriter writer = new FileWriter(Paths.get(outDir, String.format("cantilever_%s_%spx_%s_%s.json", armType, (int)px, insulatorType, placement)).toString())) {
                                writer.write(json);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        for (int width = MIN_WIDTH; width <= MAX_WIDTH; width++) {
            for (String insulatorType : insulatorTypes) {                
                BlockStates states = new BlockStates();
                for (double px : attachPixels) {
                    for (String armType : armTypes) {
                        for (String placement : insulatorPlacement) {
                            for (EFacing facing : EFacing.values()) {
                                states.variants.put(String.format("facing=%s,insulator_placement=%s,registration_arm=%s,connection=%spx", facing.name, placement, armType, (int)px), 
                                    new BlockState(String.format(NAMESPACE + ":block/cantilever/single/%s/cantilever_%s_%spx_%s_%s", width, armType, (int)px, insulatorType, placement), facing.y)
                                );
                            }
                        }
                    }
                }
                String outDir = "output/blockstates";  
                new File(outDir).mkdirs();
                String json = gson.toJson(states);
                try (FileWriter writer = new FileWriter(Paths.get(outDir, String.format("cantilever_%s_%s.json", width, insulatorType)).toString())) {
                    writer.write(json);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }



        // DOUBLE
        for (int width = 2; width <= 8; width++) {
            for (double px : attachPixels) {
                for (String armType : armTypes) {
                    String outDir =  String.format("output/cantilever/double/%s", width, armType, (int)px);  
                    String outPath = Paths.get(outDir, String.format("cantilever_%s_%spx.obj", armType, (int)px)).toString();
                    new File(outDir).mkdirs();

                    String wireArmPath = String.format("contact_wire_holder_%s.obj", armType);
                    String simpleWireArmPath = String.format("contact_wire_holder_simple_%s.obj", armType);

                    Obj obj = new Obj();
                    final int fPx = (int)px;
                    Map<Integer, Pair<InsulatorPlacement, InsulatorPlacement>> cantileverAttachments = new HashMap<>();
                    List<InsulatorPlacement> placements = new ArrayList<>();

                    if (armType.equals("outer")) {
                        placements.addAll(makeCantilever(obj, input, wireArmPath, width + 0, PIXEL * (px / 2d), ((float)width + 1.25f) / 2f, PIXEL * 9, 0, 0,
                        (top, bottom) -> {
                            cantileverAttachments.put(fPx, new Pair<>(
                                new InsulatorPlacement(new float[] { (float)(top.x - (PIXEL * 9)) * 16, (float)top.y * 16, (float)top.z * 16 }, new float[3]),
                                new InsulatorPlacement(new float[] { (float)(bottom.x - (PIXEL * 9)) * 16, (float)bottom.y * 16, (float)bottom.z * 16 }, new float[3])
                            ));
                        }));
                        placements.addAll(makeCantilever(obj, input, simpleWireArmPath, width + 0, PIXEL * (px / 2d), ((float)width + 1.25f) / 2f, PIXEL * -9, PIXEL * 7, PIXEL * 8, null));
                    } else if (armType.equals("inner")) {
                        placements.addAll(makeCantilever(obj, input, wireArmPath, width + 0, PIXEL * (px / 2d), ((float)width + 1.25f) / 2f, PIXEL * 9, PIXEL * 7, 0,
                        (top, bottom) -> {
                            cantileverAttachments.put(fPx, new Pair<>(
                                new InsulatorPlacement(new float[] { (float)(top.x - (PIXEL * 9)) * 16, (float)top.y * 16, (float)top.z * 16 }, new float[3]),
                                new InsulatorPlacement(new float[] { (float)(bottom.x - (PIXEL * 9)) * 16, (float)bottom.y * 16, (float)bottom.z * 16 }, new float[3])
                            ));
                        }));
                        placements.addAll(makeCantilever(obj, input, simpleWireArmPath, width + 0, PIXEL * (px / 2d), ((float)width + 1.25f) / 2f, PIXEL * -9, 0, PIXEL * -8, null));
                    } else if (armType.equals("center")) {
                        placements.addAll(makeCantilever(obj, input, String.format("contact_wire_holder_inner.obj", armType), width + 0, PIXEL * (px / 2d), ((float)width + 1.25f) / 2f, PIXEL * 9, PIXEL * 7, 0,
                        (top, bottom) -> {
                            cantileverAttachments.put(fPx, new Pair<>(
                                new InsulatorPlacement(new float[] { (float)(top.x - (PIXEL * 9)) * 16, (float)top.y * 16, (float)top.z * 16 }, new float[3]),
                                new InsulatorPlacement(new float[] { (float)(bottom.x - (PIXEL * 9)) * 16, (float)bottom.y * 16, (float)bottom.z * 16 }, new float[3])
                            ));
                        }));
                        placements.addAll(makeCantilever(obj, input, String.format("contact_wire_holder_outer.obj", armType), width + 0, PIXEL * (px / 2d), ((float)width + 1.25f) / 2f, PIXEL * -9, 0, PIXEL * -8, null));
                    }
                    obj.save(outPath);
                    
                    for (String insulatorType : insulatorTypes) {
                        for (String placement : insulatorPlacement) {
                            List<Additional> additional = new ArrayList<>();
                            if (placement.equals("back")) {
                                additional.addAll(List.of(
                                    new Additional(String.format(NAMESPACE + ":models/block/insulator/base/%s_insulator.obj", insulatorType), placements.get(0).rot, placements.get(0).pos, true),
                                    new Additional(String.format(NAMESPACE + ":models/block/insulator/base/%s_insulator.obj", insulatorType), placements.get(1).rot, placements.get(1).pos, true),

                                    new Additional(String.format(NAMESPACE + ":models/block/insulator/base/%s_insulator.obj", insulatorType), placements.get(5).rot, placements.get(5).pos, true),
                                    new Additional(String.format(NAMESPACE + ":models/block/insulator/base/%s_insulator.obj", insulatorType), placements.get(6).rot, placements.get(6).pos, true)
                                ));
                            } else if (placement.equals("front")) {
                                additional.addAll(List.of(
                                    new Additional(String.format(NAMESPACE + ":models/block/insulator/base/%s_insulator.obj", insulatorType), placements.get(2).rot, placements.get(2).pos, true),
                                    new Additional(String.format(NAMESPACE + ":models/block/insulator/base/%s_insulator.obj", insulatorType), placements.get(3).rot, placements.get(3).pos, true),
                                    new Additional(String.format(NAMESPACE + ":models/block/insulator/base/%s_insulator.obj", insulatorType), placements.get(4).rot, placements.get(4).pos, true),

                                    new Additional(String.format(NAMESPACE + ":models/block/insulator/base/%s_insulator.obj", insulatorType), placements.get(7).rot, placements.get(7).pos, true),
                                    new Additional(String.format(NAMESPACE + ":models/block/insulator/base/%s_insulator.obj", insulatorType), placements.get(8).rot, placements.get(8).pos, true),
                                    new Additional(String.format(NAMESPACE + ":models/block/insulator/base/%s_insulator.obj", insulatorType), placements.get(9).rot, placements.get(9).pos, true)
                                ));
                            }
                            if (cantileverAttachments.containsKey(fPx)) {
                                additional.add(new Additional(String.format(NAMESPACE + ":models/block/cantilever/post_connection/lattice_long.obj"), cantileverAttachments.get(fPx).a.rot, cantileverAttachments.get(fPx).a.pos, false));
                                additional.add(new Additional(String.format(NAMESPACE + ":models/block/cantilever/post_connection/lattice_long.obj"), cantileverAttachments.get(fPx).b.rot, cantileverAttachments.get(fPx).b.pos, false));
                            }
                            Model model = new Model(String.format(NAMESPACE + ":models/block/cantilever/double/%s/cantilever_%s_%spx.obj", width, armType, (int)px), additional);
                            
                            String json = gson.toJson(model);
                            try (FileWriter writer = new FileWriter(Paths.get(outDir, String.format("cantilever_%s_%spx_%s_%s.json", armType, (int)px, insulatorType, placement)).toString())) {
                                writer.write(json);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        for (int width = MIN_WIDTH; width <= MAX_WIDTH; width++) {
            for (String insulatorType : insulatorTypes) {                
                BlockStates states = new BlockStates();
                for (double px : attachPixels) {
                    for (String armType : armTypes) {
                        for (String placement : insulatorPlacement) {
                            for (EFacing facing : EFacing.values()) {
                                states.variants.put(String.format("facing=%s,insulator_placement=%s,registration_arm=%s,connection=%spx", facing.name, placement, armType, (int)px), 
                                    new BlockState(String.format(NAMESPACE + ":block/cantilever/double/%s/cantilever_%s_%spx_%s_%s", width, armType, (int)px, insulatorType, placement), facing.y)
                                );
                            }
                        }
                    }
                }
                String outDir = "output/blockstates";  
                new File(outDir).mkdirs();
                String json = gson.toJson(states);
                try (FileWriter writer = new FileWriter(Paths.get(outDir, String.format("cantilever_double_%s_%s.json", width, insulatorType)).toString())) {
                    writer.write(json);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static List<InsulatorPlacement> makeCantilever(Obj obj, String baseModelPath, String wireArmModelPath, double cantileverWidth, double attachOffset, double openingDistance, double xOffset, double topYOffset, double topZOffset, BiConsumer<Vector3d, Vector3d> cantileverAttachment) {
        ObjObject root = ObjObject.loadSample(baseModelPath).get(0);

        // ### Top
        ObjObject base = root.copy();
        base.scale(new Vector3d(PIXEL, PIXEL, 1));
        base.translate(new Vector3d(PIXEL * 7.5f + xOffset, PIXEL * 10, PIXEL * -8));

        Vector3d[] topRight = TranslationUtils.zRight(base.vertices);
        Vector3d[] topLeft = TranslationUtils.zLeft(base.vertices);
        
        for (int i = 0; i < topRight.length; i++) {
            Vector3d v = topRight[i];
            v.add(new Vector3d(0, 0, 1d - attachOffset));
        }
        for (int i = 0; i < topLeft.length; i++) {
            Vector3d v = topLeft[i];
            v.add(new Vector3d(0, topYOffset, -(cantileverWidth - 2) + topZOffset));
        }

        double topYDiff = -topYOffset;
        double topZDiff = topRight[0].z - topLeft[0].z;
        double topAngle = Math.toDegrees(Math.atan2(topYDiff, topZDiff));
        double topCorrectedDeltaY = DIAMETER / Math.cos(Math.toRadians(topAngle)) - DIAMETER;
        System.out.println("Angle: " + topAngle + ", YDelta: " + topCorrectedDeltaY + ", YDiff: " + topYDiff + ", ZDiff: " + topZDiff);

        for (int i = 0; i < topRight.length; i++) {
            Vector3d v = topRight[i];
            if (i == 1 || i == 3) {
                v.add(new Vector3d(0, -topCorrectedDeltaY, 0));
            }
        }
        calcVertices(topLeft[0], new Vector3d(topLeft[0]).sub(topRight[0]), PIXEL, topLeft);

        Vector3d topCenLeft = new Vector3d().add(-(topLeft[1].x - topLeft[2].x) / 2d, (topLeft[1].y - topLeft[2].y) / 2d, (topLeft[1].z - topLeft[2].z) / 2d);
        Vector3d topCenRight = new Vector3d().add(-(topRight[1].x - topRight[2].x) / 2d, (topRight[1].y - topRight[2].y) / 2d, (topRight[1].z - topRight[2].z) / 2d);
        double topArmLength = new Vector3d(topLeft[0]).sub(topRight[0]).length();
        Vector3d topCenDirection = new Vector3d(topLeft[0]).sub(topRight[0]).normalize();
        obj.objects.add(base);

        // ### Bottom
        ObjObject translated = base.copy();

        Vector3d[] right = TranslationUtils.zRight(translated.vertices);
        Vector3d[] left = TranslationUtils.zLeft(translated.vertices);

        double yDiff = -(openingDistance);
        double zDiff = right[0].z - left[0].z;
        double angle = Math.toDegrees(Math.atan2(-(openingDistance + topYOffset), zDiff));
        double correctedDeltaY = DIAMETER / Math.cos(Math.toRadians(angle)) - DIAMETER;
        System.out.println("Angle: " + angle + ", YDelta: " + correctedDeltaY + ", YDiff: " + yDiff + ", ZDiff: " + zDiff);

        for (int i = 0; i < topLeft.length; i++) { // Correct top
            Vector3d v = topLeft[i];
            if (i == 1 || i == 3) {
                double winkelBogenmass = Math.toRadians(angle - topAngle);
                double hypotenuse = (DIAMETER) / Math.sin(winkelBogenmass);
                double ankathete = Math.cos(winkelBogenmass) * hypotenuse;
                v.add(new Vector3d(topCenDirection).mul(0, ankathete, ankathete));
            }
        }

        for (int i = 0; i < right.length; i++) {
            Vector3d v = right[i];
            v.add(new Vector3d(0, yDiff, 0));
            if (i == 1 || i == 3) {
                v.add(new Vector3d(0, -correctedDeltaY, 0));
            }
        }
        calcVertices(left[0], new Vector3d(left[0]).sub(right[0]), PIXEL, left);

        Vector3d cenLeft = new Vector3d().add(-(left[1].x - left[2].x) / 2d, (left[1].y - left[2].y) / 2d, (left[1].z - left[2].z) / 2d);
        Vector3d cenRight = new Vector3d().add(-(right[1].x - right[2].x) / 2d, (right[1].y - right[2].y) / 2d, (right[1].z - right[2].z) / 2d);
        double armLength = new Vector3d(left[0]).sub(right[0]).length();
        Vector3d cenDirection = new Vector3d(left[0]).sub(right[0]).normalize();
        
        for (int i = 0; i < left.length; i++) { // Extrude
            Vector3d v = left[i];
            v.add(new Vector3d(cenDirection).mul(1, PIXEL * 0.5d, PIXEL * 0.5d));
        }

        obj.objects.add(translated);

        
        if (cantileverAttachment != null) {
            cantileverAttachment.accept(new Vector3d(topCenRight).add(topRight[0]), new Vector3d(cenRight).add(right[0]));
        }


        List<InsulatorPlacement> insulatorLocations = new ArrayList<>();
        Vector3d v;
        v = new Vector3d(right[0]).add(cenRight).add(new Vector3d(cenDirection).mul(1, 0.6f, 0.6f));
        insulatorLocations.add(new InsulatorPlacement(new float[] { (float)v.x * 16, (float)v.y * 16, (float)v.z * 16 }, new float[] { (float)-angle, 0, 0 }));
        v = new Vector3d(topRight[0]).add(topCenRight).add(new Vector3d(topCenDirection).mul(1, 0.5f, 0.5f));
        insulatorLocations.add(new InsulatorPlacement(new float[] { (float)v.x * 16, (float)v.y * 16, (float)v.z * 16 }, new float[] { (float)-topAngle, 0, 0 }));

        v = new Vector3d(topLeft[0]).add(topCenLeft).sub(new Vector3d(topCenDirection).mul(1, 0.55f + (cantileverWidth * 0.15f), 0.55f + (cantileverWidth * 0.15f)));
        insulatorLocations.add(new InsulatorPlacement(new float[] { (float)v.x * 16, (float)v.y * 16, (float)v.z * 16 }, new float[] { (float)-topAngle, 0, 0 }));
        v = new Vector3d(left[0]).add(cenLeft).sub(new Vector3d(cenDirection).mul(1, 0.8f + (cantileverWidth * 0.15f), 0.8f + (cantileverWidth * 0.15f)));
        insulatorLocations.add(new InsulatorPlacement(new float[] { (float)v.x * 16, (float)v.y * 16, (float)v.z * 16 }, new float[] { (float)-angle, 0, 0 }));

        List<ObjObject> os = ObjObject.loadSample(wireArmModelPath);
        for (int n = 0; n < os.size(); n++) {
            ObjObject o = os.get(n);
            o.translate(new Vector3d(xOffset, cantileverWidth < 3 ? 0.5f : (cantileverWidth >= 5 ? -1 : 0), -cantileverWidth + 3));
            if (n != 0) {
                obj.objects.add(o);
                continue;
            }
            Vector3d[] oRight = new Vector3d[] { o.vertices[0], o.vertices[2], o.vertices[5], o.vertices[7] };
            Vector3d[] oLeft = new Vector3d[] { o.vertices[1], o.vertices[3], o.vertices[4], o.vertices[6] };

            double oYDiff = -(oRight[0].y - oLeft[0].y);
            double oZDiff = oRight[0].z - oLeft[0].z;
            double oAngle = Math.toDegrees(Math.atan2(oYDiff, oZDiff));
            double oCorrectedDeltaY = DIAMETER / Math.cos(Math.toRadians(oAngle)) - DIAMETER;
            System.out.println("Angle: " + oAngle + ", YDelta: " + oCorrectedDeltaY + ", YDiff: " + oYDiff + ", ZDiff: " + oZDiff);


            Vector3d oCenLeft = new Vector3d().add(-(oLeft[1].x - oLeft[2].x) / 2d, (oLeft[1].y - oLeft[2].y) / 2d, (oLeft[1].z - oLeft[2].z) / 2d);
            Vector3d oCenRight = new Vector3d().add(-(oRight[1].x - oRight[2].x) / 2d, (oRight[1].y - oRight[2].y) / 2d, (oRight[1].z - oRight[2].z) / 2d);
            double oArmLength = new Vector3d(oLeft[0]).sub(oRight[0]).length();
            Vector3d oCenDirection = new Vector3d(oLeft[0]).sub(oRight[0]).normalize();
            
            for (int i = 0; i < oRight.length; i++) { // Extrude
                Vector3d a = oRight[i];
                Vector3d result = intersectRayWithPolygon(a, new Vector3d(oCenDirection).negate(), TranslationUtils.yBottom(translated.vertices));
                if (result != null) {        
                    a.set(result);               
                } else {
                    result = intersectRayWithPolygon(a, new Vector3d(oCenDirection), TranslationUtils.yBottom(translated.vertices));
                    if (result != null) {
                        a.set(result);
                    }
                }
            }
            obj.objects.add(o);

            boolean simple = wireArmModelPath.contains("simple");
            boolean outer = !simple && wireArmModelPath.contains("outer");
            v = new Vector3d(oLeft[0]).add(oCenLeft).sub(new Vector3d(oCenDirection).mul(1, 1.25d + (outer ? 0.5d : 0) - (simple ? 0.5d : 0), 1.25d + (outer ? 0.5d : 0) - (simple ? 0.5d : 0)));
            insulatorLocations.add(new InsulatorPlacement(new float[] { (float)v.x * 16, (float)v.y * 16, (float)v.z * 16 }, new float[] { (float)oAngle, 0, 0 }));
        }

        return insulatorLocations;
    }

    static Vector3d intersectRayWithPolygon(Vector3d point, Vector3d direction, Vector3d[] plane) {

        Vector3d v1 = plane[0];
        Vector3d v2 = plane[1];
        Vector3d v3 = plane[2];
        Vector3d v4 = plane[3];

        Vector3d edge1 = new Vector3d(v2).sub(v1);
        Vector3d edge2 = new Vector3d(v3).sub(v1);
        Vector3d normal = edge1.cross(edge2, new Vector3d()).normalize();

        double d = -normal.dot(v1);
        double nd = normal.dot(direction);
        if (Math.abs(nd) < 1e-6) {
            return null;
        }
        double t = -(normal.dot(point) + d) / nd;
        if (t < 0) {
            return null;
        }

        Vector3d p = new Vector3d(direction).mul(t).add(point);
        if (isPointInTriangle(p, v1, v2, v3) || isPointInTriangle(p, v1, v3, v4)) {
            return p;
        }
        return null;
    }

    static boolean isPointInTriangle(Vector3d p, Vector3d a, Vector3d b, Vector3d c) {
        Vector3d v0 = new Vector3d(c).sub(a);
        Vector3d v1 = new Vector3d(b).sub(a);
        Vector3d v2 = new Vector3d(p).sub(a);

        double dot00 = v0.dot(v0);
        double dot01 = v0.dot(v1);
        double dot02 = v0.dot(v2);
        double dot11 = v1.dot(v1);
        double dot12 = v1.dot(v2);

        double denom = (dot00 * dot11 - dot01 * dot01);
        if (Math.abs(denom) < 1e-6) {
            return false;
        }

        double u = (dot11 * dot02 - dot01 * dot12) / denom;
        double v = (dot00 * dot12 - dot01 * dot02) / denom;

        return (u >= 0) && (v >= 0) && (u + v <= 1);
    }

    static Vector3d calcVertices(Vector3d point, Vector3d direction, double thickness, Vector3d[] arr) {
		Vector3d norm = new Vector3d(direction).normalize();
		Vector3d rightVec = new Vector3d(norm.z, 0, -norm.x).normalize().mul(thickness);
		Vector3d upVec = new Vector3d(norm).cross(rightVec).normalize().mul(thickness);
		
        arr[1].set(new Vector3d().add(point).sub(upVec));
        arr[3].set(new Vector3d().add(point).sub(upVec).add(rightVec));

        return new Vector3d().sub(new Vector3d(upVec).sub(rightVec).mul(0.5f));
	}


    static class Obj {
        List<ObjObject> objects = new ArrayList<>();

        void save(String path) {
            String mtlName = new File(path).getName().replace(".obj", ".mtl");
            Map<String, String> mtls = new HashMap<>();

            try (BufferedWriter objWriter = new BufferedWriter(new FileWriter(path, false));
                 BufferedWriter mtlWriter = new BufferedWriter(new FileWriter(Paths.get(new File(path).getAbsoluteFile().getParent(), mtlName).toString(), false))
            ) {
                objWriter.write("mtllib " + mtlName);
                objWriter.newLine();
                objWriter.newLine();

                for (int i = 0; i < objects.size(); i++) {
                    ObjObject o = objects.get(i);
                    objWriter.write(o.toString(i));

                    mtls.computeIfAbsent(o.mtlName, x -> o.mtlMaterialPath);
                }

                for (var e : mtls.entrySet()) {                    
                    mtlWriter.write("newmtl " + e.getKey());
                    mtlWriter.newLine();
                    mtlWriter.write("map_Kd " + e.getValue());
                    mtlWriter.newLine();
                }
            } catch(IOException ex){
                ex.printStackTrace();
            }
        }
    }


    static class TranslationUtils {

        static Vector3d rotateAroundPivot(Vector3d vector, Vector3d pivot, double angle, Vector3d axis) {
            Vector3d translated = new Vector3d(vector).sub(pivot);
            Matrix4d rotationMatrix = new Matrix4d().rotate(angle, axis);
            Vector3d rotated = new Vector3d();
            rotationMatrix.transformPosition(translated, rotated);
            rotated.add(pivot);

            return rotated;
        }

        static Vector3d[] xLeft(Vector3d[] src) {
            Vector3d[] r = new Vector3d[src.length / 2];
            System.arraycopy(src, 4, r, 0, 4);
            return r;
        }

        static Vector3d[] xRight(Vector3d[] src) {
            Vector3d[] r = new Vector3d[src.length / 2];
            System.arraycopy(src, 0, r, 0, 4);
            return r;
        }

        static Vector3d[] yBottom(Vector3d[] src) {
            Vector3d[] r = new Vector3d[src.length / 2];
            int j = 0;
            for (int i = 0; i < src.length; i++) {
                if (i % 4 == 2 || i % 4 == 3) {
                    r[j++] = src[i];
                }
            }
            return r;
        }

        static Vector3d[] yTop(Vector3d[] src) {
            Vector3d[] r = new Vector3d[src.length / 2];
            int j = 0;
            for (int i = 0; i < src.length; i++) {
                if (i % 4 == 0 || i % 4 == 1) {
                    r[j++] = src[i];
                }
            }
            return r;
        }

        static Vector3d[] zLeft(Vector3d[] src) {
            Vector3d[] r = new Vector3d[4];
            for (int i = 1; i < src.length; i += 2) {
                r[i / 2] = src[i];
            }
            return r;
        }
        
        static Vector3d[] zRight(Vector3d[] src) {
            Vector3d[] r = new Vector3d[4];
            for (int i = 0; i < src.length; i += 2) {
                r[i / 2] = src[i];
            }
            return r;
        }
    }

    static class ObjObject {
        String name = UUID.randomUUID().toString();
        Vector3d[] vertices;
        Vector2d[] textureCoords;
        Vector3d[] normals;
        String mtlName;
        String mtlMaterialPath;
        Face[] faces;

        public ObjObject(Vector3d[] vertices, Vector2d[] textureCoords, Vector3d[] normals, String mtlName, String mtlMaterialPath, Face[] faces) {
            this.vertices = vertices;
            this.textureCoords = textureCoords;
            this.normals = normals;
            this.mtlName = mtlName;
            this.mtlMaterialPath = mtlMaterialPath;
            this.faces = faces;
        }

        void translate(Vector3d trans) {
            for (Vector3d v : vertices) {
                v.add(trans);
            }
        }
        
        void scale(Vector3d scale) {
            for (Vector3d v : vertices) {
                v.mul(scale);
            }
        }        

        ObjObject copy() {
            ObjObjectBuilder builder = new ObjObjectBuilder();
            for (Vector3d v : vertices) {
                builder.vertices.add(new Vector3d(v.x, v.y, v.z));
            }
            for (Vector2d v : textureCoords) {
                builder.textureCoords.add(new Vector2d(v.x, v.y));
            }
            for (Vector3d v : normals) {
                builder.normals.add(new Vector3d(v.x, v.y, v.z));
            }
            for (Face v : faces) {
                Face f = new Face();
                for (int i = 0; i < v.faceDefs.length; i++) {
                    FaceDef original = v.faceDefs[i];
                    f.faceDefs[i] = FaceDef.of(original.toString(0));
                }
                builder.faces.add(f);
            }
            builder.mtlName = mtlName;
            builder.mtlMaterialPath = mtlMaterialPath;
            return builder.build();
        }

        static List<ObjObject> loadSample(String path) {
            List<ObjObject> objs = new ArrayList<>();
            boolean started = false;
            ObjObjectBuilder builder = new ObjObjectBuilder();

            Map<String, String> mtls = new HashMap<>();            

            final Function<String, String> getMtl = (in) -> {
                if (mtls.containsKey(in)) {
                    return mtls.get(in);
                }
                System.err.println("Not found: " + in);
                return "";
            };

            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] c = line.split(" ");
                    switch (c[0]) {
                        case "mtllib":
                            mtls.clear();
                            try (BufferedReader mtlBr = new BufferedReader(new FileReader(Paths.get(new File(path).getAbsoluteFile().getParent(), c[1]).toString()))) {
                                String l;
                                boolean newmtl = false;
                                String newmtlName = "";
                                while ((l = mtlBr.readLine()) != null) {
                                    String[] mtlc = l.split(" ");
                                    switch (mtlc[0]) {
                                        case "newmtl":
                                            newmtlName = mtlc[1];
                                            newmtl = true;
                                            break;
                                        case "map_Kd":
                                            if (newmtl) {
                                                mtls.put(newmtlName, mtlc[1]);
                                            }
                                            newmtl = false;
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            } catch (Throwable e) {
                                e.printStackTrace(); 
                            }

                            break;
                        case "o":
                            if (started) {
                                objs.add(builder.build());
                                builder = new ObjObjectBuilder();
                            }
                            started = true;                            
                            break;
                        case "v":
                            builder.vertices.add(new Vector3d(Double.parseDouble(c[1]), Double.parseDouble(c[2]), Double.parseDouble(c[3])));
                            break;
                        case "vt":
                            builder.textureCoords.add(new Vector2d(Double.parseDouble(c[1]), Double.parseDouble(c[2])));
                            break;
                        case "vn":
                            builder.normals.add(new Vector3d(Double.parseDouble(c[1]), Double.parseDouble(c[2]), Double.parseDouble(c[3])));
                            break;
                        case "usemtl":
                            builder.mtlName = c[1];
                            builder.mtlMaterialPath = getMtl.apply(builder.mtlName);
                            break;
                        case "f":
                            Face f = new Face();
                            f.faceDefs[0] = FaceDef.of(c[1]);
                            f.faceDefs[1] = FaceDef.of(c[2]);
                            f.faceDefs[2] = FaceDef.of(c[3]);
                            f.faceDefs[3] = FaceDef.of(c[4]);
                            builder.faces.add(f);
                            break;
                        default:
                            break;
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace(); 
            }
            objs.add(builder.build());
            return objs;
        }

        static class ObjObjectBuilder {            
            List<Vector3d> vertices = new ArrayList<Vector3d>(8);
            List<Vector2d> textureCoords = new ArrayList<Vector2d>(24);
            List<Vector3d> normals = new ArrayList<Vector3d>(6);
            String mtlName = UUID.randomUUID().toString();
            String mtlMaterialPath = "";
            List<Face> faces = new ArrayList<Face>(6);

            ObjObject build() {
                return new ObjObject(vertices.toArray(Vector3d[]::new), textureCoords.toArray(Vector2d[]::new), normals.toArray(Vector3d[]::new), mtlName, mtlMaterialPath, faces.toArray(Face[]::new));
            }
        }

        void recalcNormals(Vector3d[] vertices, Face[] faces) {
    
            // Schleife über jedes Face
            for (int i = 0; i < faces.length; i++) {
                // Indizes der Vertices für das aktuelle Face
                int v0Index = faces[i].faceDefs[0].vertex % 8;
                int v1Index = faces[i].faceDefs[1].vertex % 8;
                int v2Index = faces[i].faceDefs[2].vertex % 8;
    
                // Vertices des aktuellen Faces abrufen
                Vector3d v0 = vertices[v0Index];
                Vector3d v1 = vertices[v1Index];
                Vector3d v2 = vertices[v2Index];
    
                // Berechnung der Vektoren von v0 zu v1 und von v0 zu v2
                Vector3d edge1 = new Vector3d();
                v1.sub(v0, edge1);
    
                Vector3d edge2 = new Vector3d();
                v2.sub(v0, edge2);
    
                // Kreuzprodukt von edge1 und edge2 berechnen, um die Normale zu erhalten
                Vector3d normal = new Vector3d();
                edge1.cross(edge2, normal);
    
                // Normalisieren der Normalen, damit sie die Länge 1 hat
                normal.normalize();
    
                // Normale im Array speichern
                normals[i] = normal;
            }
        }

        public String toString(int i) {
            //recalcNormals(vertices, faces);
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("o %s\n", name));
            for (Vector3d v : vertices) {
                sb.append(String.format("v %s %s %s\n", v.x, v.y, v.z));
            }
            for (Vector2d v : textureCoords) {
                sb.append(String.format("vt %s %s\n", v.x, v.y));
            }
            for (Vector3d v : normals) {
                sb.append(String.format("vn %s %s %s\n", v.x, v.y, v.z));
            }
            sb.append(String.format("usemtl %s\n", mtlName));
            for (Face v : faces) {
                sb.append(String.format("f %s\n", v.toString(i)));
            }
            return sb.toString();
        }
    }

    static class FaceDef {

        int vertex;
        int texture;
        int normal;

        public String toString(int i) {
            return String.format("%s/%s/%s", vertex + (i * 8), texture + (i * 24), normal + (i * 6));
        }

        static FaceDef of(String s) {            
            String[] c = s.split("/");
            FaceDef f = new FaceDef();
            f.vertex = (Integer.parseInt(c[0]) - 1) % 8 + 1;
            f.texture = (Integer.parseInt(c[1]) - 1) % 24 + 1;
            f.normal = (Integer.parseInt(c[2]) - 1) % 6 + 1;
            return f;
        }
    }

    static class Face {
        FaceDef[] faceDefs = new FaceDef[4];

        public String toString(int i) {
            return String.format("%s %s %s %s", faceDefs[0].toString(i), faceDefs[1].toString(i), faceDefs[2].toString(i), faceDefs[3].toString(i));
        }
    }

    static enum Axis {
        X(new Vector3d(1, 0, 0)),
        Y(new Vector3d(0, 1, 0)),
        Z(new Vector3d(0, 0, 1));

        final Vector3d v;

        Axis(Vector3d v) {
            this.v = v;
        }

        Vector3d mul(Vector3d trans) {
            return trans.mul(v);
        }
    }

    public static class InsulatorPlacement {
        public float[] pos;
        public float[] rot;
        public InsulatorPlacement(float[] pos, float[] rot) {
            this.pos = pos;
            this.rot = rot;
        }
    }


    public static class Model {
        String loader = NAMESPACE + ":multipart_obj";
        boolean automatic_culling = true;
        boolean shade_quads = true;
        boolean flip_v = true;
        boolean emissive_ambient = false;
        String model;
        Texture textures = new Texture();
        List<Additional> add;

        public Model(String modelpath, List<Additional> additional) {
            this.model = modelpath;
            this.add = additional;
        }

        public static class Texture {
            String particle = NAMESPACE + ":block/metal";
        }

        public static class Additional {
            String model;
            float[] rotation;
            float[] offset;
            boolean inheritable = true;

            public Additional(String model, float[] rotation, float[] offset, boolean inheritable) {
                this.model = model;
                this.rotation = rotation;
                this.offset = offset;
                this.inheritable = inheritable;
            }
        }
    }
    
}
