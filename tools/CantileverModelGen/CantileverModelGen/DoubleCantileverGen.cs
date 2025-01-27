using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CantileverModelGen
{
    internal class DoubleCantileverGen
    {
        public static void run(string srcPath)
        {
            Output o = new Output();
            string[] types = new string[] {"inner", "outer"};
            string[] connections = new string[] {"16px", "12px", "8px", "5px", "4px"};
            foreach (var sizeDir in Directory.GetDirectories(srcPath))
            {
                string size = new DirectoryInfo(sizeDir).Name;

                if (!int.TryParse(size, out _))
                {
                    continue;
                }

                o.output.Add(size, new Dictionary<string, Dictionary<string, Dictionary<string, Dictionary<string, Dictionary<string, AdditionalModel[]>>>>>());
                o.output[size].Add("default", new Dictionary<string, Dictionary<string, Dictionary<string, Dictionary<string, AdditionalModel[]>>>>());
                o.output[size]["default"].Add("default", new Dictionary<string, Dictionary<string, Dictionary<string, AdditionalModel[]>>>());

                // BACK
                o.output[size]["default"]["default"].Add("back", new Dictionary<string, Dictionary<string, AdditionalModel[]>>());
                foreach (var type in types)
                {
                    o.output[size]["default"]["default"]["back"].Add(type, new Dictionary<string, AdditionalModel[]>());
                    foreach (var connection in connections)
                    {
                        string selectedFile = Directory.GetFiles(sizeDir).Where(x => x.Contains(type) && x.Contains(connection)).FirstOrDefault();
                        Console.WriteLine(type + ", " + connection + ": " + selectedFile);
                        string json = File.ReadAllText(selectedFile);
                        BBModel model = JsonConvert.DeserializeObject<BBModel>(json);
                        List<AdditionalModel> additionalModels = new List<AdditionalModel>();

                        additionalModels.Add(new AdditionalModel()
                        {
                            model = "<cantilever>",
                            offset = new float[] { 9, 0, 0 },
                            rotation = new float[] { 0, 0, 0 }
                        });

                        BBModelElement el10 = model.elements.Where(x => x.name == "m1").FirstOrDefault();
                        BBModelElement modelEl1 = model.elements.Where(x => x.name == "i11").First();
                        float[] rot11 = FindStringAndSumRotations(JObject.Parse(json)["outliner"], modelEl1.uuid);
                        rot11[0] += modelEl1.rotation[0];
                        rot11[1] += modelEl1.rotation[1];
                        rot11[2] += modelEl1.rotation[2];
                        additionalModels.Add(new AdditionalModel()
                        {
                            model = "<insulator>",
                            offset = new float[] { -1f, el10.from[1], el10.from[2] },
                            rotation = new float[] { rot11[0], rot11[1], rot11[2] }
                        });
                        BBModelElement el20 = model.elements.Where(x => x.name == "m2").FirstOrDefault();
                        BBModelElement modelEl2 = model.elements.Where(x => x.name == "i21").First();
                        float[] rot21 = FindStringAndSumRotations(JObject.Parse(json)["outliner"], modelEl2.uuid);
                        rot21[0] += modelEl2.rotation[0];
                        rot21[1] += modelEl2.rotation[1];
                        rot21[2] += modelEl2.rotation[2];
                        additionalModels.Add(new AdditionalModel()
                        {
                            model = "<insulator>",
                            offset = new float[] { -1f, el20.from[1], el20.from[2] },
                            rotation = new float[] { rot21[0], rot21[1], rot21[2] }
                        });

                        foreach (var e in model.elements.Where(x => x.name.StartsWith("ext")))
                        {
                            additionalModels.Add(new AdditionalModel()
                            {
                                model = "<post_connection>",
                                offset = new float[] { e.origin[0], e.origin[1], e.origin[2] },
                                rotation = new float[] { 0, 0, 0 }
                            });
                        }


                        o.output[size]["default"]["default"]["back"][type].Add(connection, additionalModels.ToArray());
                    }
                }

                // FRONT
                o.output[size]["default"]["default"].Add("front", new Dictionary<string, Dictionary<string, AdditionalModel[]>>());
                foreach (var type in types)
                {
                    o.output[size]["default"]["default"]["front"].Add(type, new Dictionary<string, AdditionalModel[]>());
                    foreach (var connection in connections)
                    {
                        string json = File.ReadAllText(Directory.GetFiles(sizeDir).Where(x => x.Contains(type) && x.Contains(connection)).First());
                        BBModel model = JsonConvert.DeserializeObject<BBModel>(json);
                        List<AdditionalModel> additionalModels = new List<AdditionalModel>();

                        additionalModels.Add(new AdditionalModel()
                        {
                            model = "<cantilever>",
                            offset = new float[] { 9, 0, 0 },
                            rotation = new float[] { 0, 0, 0 }
                        });

                        BBModelElement el10 = model.elements.Where(x => x.name == "m3").FirstOrDefault();
                        BBModelElement modelEl1 = model.elements.Where(x => x.name == "i13").First();
                        float[] rot11 = FindStringAndSumRotations(JObject.Parse(json)["outliner"], modelEl1.uuid);
                        rot11[0] += modelEl1.rotation[0];
                        rot11[1] += modelEl1.rotation[1];
                        rot11[2] += modelEl1.rotation[2];
                        additionalModels.Add(new AdditionalModel()
                        {
                            model = "<insulator>",
                            offset = new float[] { -1f, el10.from[1], el10.from[2] },
                            rotation = new float[] { rot11[0], rot11[1], rot11[2] }
                        });
                        BBModelElement el20 = model.elements.Where(x => x.name == "m4").FirstOrDefault();
                        BBModelElement modelEl2 = model.elements.Where(x => x.name == "i22").First();
                        float[] rot21 = FindStringAndSumRotations(JObject.Parse(json)["outliner"], modelEl2.uuid);
                        rot21[0] += modelEl2.rotation[0];
                        rot21[1] += modelEl2.rotation[1];
                        rot21[2] += modelEl2.rotation[2];
                        additionalModels.Add(new AdditionalModel()
                        {
                            model = "<insulator>",
                            offset = new float[] { -1f, el20.from[1], el20.from[2] },
                            rotation = new float[] { rot21[0], rot21[1], rot21[2] }
                        });
                        BBModelElement el30 = model.elements.Where(x => x.name == "m5").FirstOrDefault();
                        BBModelElement modelEl3 = model.elements.Where(x => x.name == "i31").First();
                        float[] rot31 = FindStringAndSumRotations(JObject.Parse(json)["outliner"], modelEl3.uuid);
                        rot31[0] += modelEl3.rotation[0];
                        rot31[1] += modelEl3.rotation[1];
                        rot31[2] += modelEl3.rotation[2];
                        additionalModels.Add(new AdditionalModel()
                        {
                            model = "<insulator>",
                            offset = new float[] { -1f, el30.from[1], el30.from[2] },
                            rotation = new float[] { rot31[0], rot31[1], rot31[2] }
                        });

                        foreach (var e in model.elements.Where(x => x.name.StartsWith("ext")))
                        {
                            additionalModels.Add(new AdditionalModel()
                            {
                                model = "<post_connection>",
                                offset = new float[] { e.origin[0], e.origin[1], e.origin[2] },
                                rotation = new float[] { 0, 0, 0 }
                            });
                        }


                        o.output[size]["default"]["default"]["front"][type].Add(connection, additionalModels.ToArray());
                    }
                }
            }

            File.WriteAllText("double_cantilever.json", JsonConvert.SerializeObject(o.output));
        }

        static float[] FindStringAndSumRotations(JToken current, string searchString, float[] parentRotation = null)
        {
            if (parentRotation == null)
            {
                parentRotation = new float[3];
            }

            if (current is JArray array)
            {
                foreach (var item in array)
                {
                    float[] result = FindStringAndSumRotations(item, searchString, parentRotation);
                    if (result != null)
                    {
                        return result;
                    }
                }
            }
            else if (current is JObject obj)
            {
                // Check if the current object is an OutlinerObject
                float[] currentRotation = obj["rotation"]?.ToObject<float[]>() ?? new float[3];

                // Add parent's rotation to current rotation
                float[] newParentRotation = new float[3];
                for (int i = 0; i < 3; i++)
                {
                    newParentRotation[i] = parentRotation[i] + currentRotation[i];
                }

                // Check the "children" array
                if (obj["children"] != null)
                {
                    float[] result = FindStringAndSumRotations(obj["children"], searchString, newParentRotation);
                    if (result != null)
                    {
                        return result;
                    }
                }
            }
            else if (current is JValue value)
            {
                // Check if the value matches the search string
                if (value.Type == JTokenType.String && (string)value == searchString)
                {
                    return parentRotation;
                }
            }

            return null; // String not found in this branch
        }
    }

    internal class Output
    {
        public Dictionary<string /* size */, Dictionary<string /* insulator_type */, Dictionary<string /* facing */, Dictionary<string /* insulator_placement */, Dictionary<string /* type */, Dictionary<string /* connection */, AdditionalModel[]>>>>>> output = new Dictionary<string, Dictionary<string, Dictionary<string, Dictionary<string, Dictionary<string, Dictionary<string, AdditionalModel[]>>>>>>();
    }

    internal class BBModel
    {
        public BBModelElement[] elements {  get; set; }



    }

    internal class BBModelElement
    {
        public string name { get; set; }
        public string uuid { get; set; }
        public float[] from { get; set; }
        public float[] origin { get; set; }
        public float[] rotation { get; set; } = new float[] {0, 0, 0};
    }
}
