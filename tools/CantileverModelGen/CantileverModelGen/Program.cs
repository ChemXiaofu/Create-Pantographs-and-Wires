using CantileverModelGen;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.ExceptionServices;
using System.Security;

class Program
{
    public class SubModel
    {
        public string inherits {  get; set; }
        public float[] rotation { get; set; }
        public float[] offset { get; set; }
        public string model { get; set; } = "";
        public bool inheritable { get; set; } = true;

        public override string ToString()
        {
            return $"Rotation: {rotation}, Offset: {offset}";
        }
    }

    public class KeyPath
    {
        public Dictionary<string, string> path { get; set; } = new Dictionary<string, string>();

        public override string ToString()
        {
            return string.Join(",", path.Select(kv => $"{kv.Key}={kv.Value}"));
        }

        public override bool Equals(object obj)
        {
            if (obj is KeyPath other)
            {
                if (this.path.Count != other.path.Count)
                    return false;

                foreach (var kvp in this.path)
                {
                    if (!other.path.ContainsKey(kvp.Key) || other.path[kvp.Key] != kvp.Value)
                        return false;
                }

                return true;
            }

            return false;
        }

        public override int GetHashCode()
        {
            int hashCode = 0;
            foreach (var kvp in path)
            {
                hashCode ^= kvp.Key.GetHashCode();
                hashCode ^= kvp.Value.GetHashCode();
            }
            return hashCode;
        }

        public KeyPath copy()
        {
            return new KeyPath()
            {
                path = new Dictionary<string, string>(path)
            };
        }
    }

    public class Variant
    {
        public KeyPath blocks { get; set; }
        public KeyPath states { get; set; }
        public SubModel[] value { get; set; }
    }

    public class ModelData
    {
        public string directory { get; set; }
        public string filename { get; set; }
        public Variant data { get; set; }

        public string FileSystemPath()
        {
            return "models/" + directory + "/" + filename + ".json";
        }

        public string ModelPath()
        {
            return "<name_space>:" + directory + "/" + filename;
        }
    }


    public static Input input;

    public static void Main(string[] args)
    {

        if (args.Length > 0)
        {
            switch (args[0])
            {
                case "double_cantilever":
                    if (args.Length < 2)
                    {
                        Console.Error.WriteLine("Missing parameters! Required: double_cantilever [srcPath]");
                        Console.ReadKey();
                        return;
                    }
                    DoubleCantileverGen.run(args[1]);
                    return;
                default:
                    throw new Exception("Invalid arg: " + args[0]);
            }
        }

        if (!File.Exists("input.json"))
        {
            Console.Error.WriteLine("There is no 'input.json' file.");
            Console.ReadKey();
        }

        string json = File.ReadAllText("input.json");
        input = JsonConvert.DeserializeObject<Input>(json);
        input.Init();


        var resultsCat = new Dictionary<KeyPath, JToken>();
        var results = new Dictionary<KeyPath, SubModel[]>();
        var dataResult = new List<Variant>();

        ValidateAndExtract(input.blocks, null, input.definitions, new KeyPath(), resultsCat);

        foreach (var v in resultsCat)
        {
            ValidateAndExtract(input.states, null, v.Value, new KeyPath(), results);
            foreach (var kvp in results)
            {
                foreach (SubModel model in kvp.Value)
                {
                    model.model = input.ReplacePlaceholders(model.model, new List<KeyPath>() { v.Key, kvp.Key });
                }
                
                dataResult.Add(new Variant()
                {
                    blocks = v.Key,
                    states = kvp.Key,
                    value = kvp.Value
                });
            }
        }

        foreach (var kvp in dataResult)
        {
            Console.WriteLine($"Block: {kvp.blocks}, State: {kvp.states}, Value: {kvp.value.Length}");
        }
        Console.WriteLine($"Created {dataResult.Count} entries!");

        Dictionary<KeyPath /* block */, Dictionary<KeyPath /* state */, ModelData>> models = new Dictionary<KeyPath, Dictionary<KeyPath, ModelData>>();
        foreach (var a in dataResult)
        {
            Dictionary<KeyPath, ModelData> states;
            if (models.ContainsKey(a.blocks))
            {
                states = models[a.blocks];
            }
            else
            {
                states = new Dictionary<KeyPath, ModelData>();
                models[a.blocks] = states;
            }

            states.Add(a.states, new ModelData()
            {
                data = a,
                directory = input.ReplacePlaceholders(input.json_directory, new List<KeyPath>() { a.blocks, a.states }),
                filename = input.ReplacePlaceholders(input.json_filename, new List<KeyPath>() { a.blocks, a.states })
            });
        }

        int i = 0;
        foreach (var a in models)
        {
            Directory.CreateDirectory("output/blockstates");
            Blockstates states = new Blockstates();

            foreach (var b in a.Value)
            {
                states.variants.Add(b.Key.ToString(), new global::Variant()
                {
                    model = input.ReplacePlaceholders(b.Value.ModelPath(), new List<KeyPath>() { a.Key, b.Key }),
                    y = b.Key.path.ContainsKey("facing") ? Facing.GetByName(b.Key.path["facing"]).rotation : 0
                });

                List<AdditionalModel> additional = new List<AdditionalModel>();
                foreach (var c in b.Value.data.value)
                {
                    additional.Add(new AdditionalModel()
                    {
                        model = input.ReplacePlaceholders(c.model, new List<KeyPath>() { a.Key, b.Key }),
                        offset = c.offset,
                        rotation = c.rotation,
                        inheritable = c.inheritable,
                    });
                }                

                BaseModel model = new BaseModel()
                {
                    ambientocclusion = input.ambientocclusion,
                    flipv = input.flipv,
                    loader = input.model_loader,
                    model = input.ReplacePlaceholders(input.base_model, new List<KeyPath>() { a.Key, b.Key }),
                    textures = new Textures()
                    {
                        particle = input.ReplacePlaceholders(input.particle_texture, new List<KeyPath>() { a.Key, b.Key })
                    },
                    add = additional.ToArray()
                };

                string path = "output/models/" + input.ReplacePlaceholders(input.json_directory, new List<KeyPath>() { a.Key, b.Key });
                Directory.CreateDirectory(path);
                string modelFile = path + "/" + input.ReplacePlaceholders(input.json_filename, new List<KeyPath>() { a.Key, b.Key }) + ".json";
                File.WriteAllText(modelFile, JsonConvert.SerializeObject(model, Formatting.Indented));
                
                Console.WriteLine("Created model file: " + modelFile);
                i++;
            }

            string blockstatesFile = "output/blockstates/" + input.ReplacePlaceholders(input.blockstates_filename, new List<KeyPath>() { a.Key }) + ".json";
            File.WriteAllText(blockstatesFile, JsonConvert.SerializeObject(states, Formatting.Indented));
            Console.WriteLine("Created blockstates file: " + blockstatesFile);
            i++;
        }

        Console.WriteLine("");
        Console.WriteLine($"Finished! Created {i} files.");
        Console.ReadKey();
    }

    static void ValidateAndExtract<T>(
        Dictionary<string, string[]> data,
        JObject lastDefinitions,
        JToken definitions,
        KeyPath currentPath,
        Dictionary<KeyPath, T> results) 
    {
        if (currentPath.path.Count == data.Count)
        {
            if (definitions.Type != JTokenType.Object && definitions.Type != JTokenType.Array)
            {
                throw new Exception($"Error: Expected object or array, but found: {definitions}");
            }

            var myObject = definitions.ToObject<T>();
            if (myObject == null)
            {
                throw new Exception($"Error: Invalid object definition: {definitions}");
            }

            if (myObject is SubModel[] v)
            {
                List<SubModel> list = new List<SubModel>();
                List<string> inheritsFrom = new List<string>();
                bool b = false;

                foreach (var a in v)
                {
                    if (!string.IsNullOrEmpty(a.inherits))
                    {
                        b = true;
                        inheritsFrom.Add(a.inherits);
                    } else
                    {
                        list.Add(a);
                    }
                }

                if (b)
                {

                    foreach (string s in inheritsFrom)
                    {
                        if (lastDefinitions.TryGetValue(s, out var defaultVal))
                        {
                            if (defaultVal.Type != JTokenType.Object && defaultVal.Type != JTokenType.Array)
                            {
                                throw new Exception($"Error: Expected object or array, but found: {defaultVal}");
                            }

                            var myObject2 = defaultVal.ToObject<SubModel[]>();
                            if (myObject2 == null)
                            {
                                throw new Exception($"Error: Invalid object definition: {defaultVal}");
                            }

                            if (myObject2 is SubModel[] v2)
                            {
                                foreach (var a2 in v2)
                                {
                                    if (string.IsNullOrEmpty(a2.inherits))
                                    {
                                        list.Add(a2);
                                    }
                                }
                            }
                        }
                    }
                }
                myObject = (T)(Object)list.ToArray();
            }

            results[currentPath] = myObject;
            return;
        }

        // Nächster Key aus `data`
        string currentKey = data.Keys.ElementAt(currentPath.path.Count);
        if (definitions.Type != JTokenType.Object)
        {
            throw new Exception($"Error: Expected object, but found: {definitions}");
        }

        var currentDefinitions = (JObject)definitions;

        // Werte des aktuellen Keys durchgehen
        foreach (var value in data[currentKey])
        {
            var nextPath = currentPath.copy();
            nextPath.path.Add(currentKey, value);

            // Wenn der spezifische Schlüssel existiert, diesen verwenden
            if (currentDefinitions.TryGetValue(value, out var nextDefinitions))
            {
                ValidateAndExtract(data, currentDefinitions, nextDefinitions, nextPath, results);
            }
            // Ansonsten den "default"-Eintrag verwenden
            else if (currentDefinitions.TryGetValue("default", out var defaultDefinitions))
            {
                ValidateAndExtract(data, currentDefinitions, defaultDefinitions, nextPath, results);
            }
            else
            {
                throw new Exception($"Error: Neither specific key '{value}' nor 'default' found in 'definitions' for {string.Join(",", nextPath)}");
            }
        }
    }
}
