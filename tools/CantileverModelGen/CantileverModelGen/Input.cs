using CantileverModelGen;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using static Program;

internal class Input
{
    public Dictionary<string, string> placeholders { get; set; }
    public Dictionary<string, string[]> blocks { get; set; }
    public Dictionary<string, string[]> states { get; set; }
    public JObject definitions { get; set; }
    public string name_space { get; set; }
    public string base_filename { get; set; }
    public string base_model { get; set; }
    public string json_directory { get; set; }
    public string json_filename { get; set; }
    public string blockstates_filename { get; set; }
    public string model_loader { get; set; }
    public string particle_texture { get; set; }
    public bool ambientocclusion { get; set; }
    public bool flipv { get; set; }

    public void Init()
    {
        if (states.ContainsKey("facing"))
        {
            states["facing"] = Facing.Values().Select(x => x.name).ToArray();
        }
    }

    public string ReplacePlaceholders(string input, List<KeyPath> paths)
    {
        input = input
            .Replace("<name_space>", name_space)
            .Replace("<base_filename>", base_filename)
        ;
        foreach (var placeholder in Program.input.placeholders)
        {
            input = input.Replace("<" + placeholder.Key + ">", placeholder.Value);
        }

        foreach (KeyPath p in paths)
        {
            foreach(var placeholder in p.path)
            {
                input = input.Replace("<" + placeholder.Key + ">", placeholder.Value);
            }
        }
        return input;
    }

}
