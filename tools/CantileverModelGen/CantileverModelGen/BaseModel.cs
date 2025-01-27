
using Newtonsoft.Json;

public class BaseModel
{
    public string loader { get; set; }
    public bool ambientocclusion { get; set; }

    [JsonProperty(PropertyName = "flip-v")]
    public bool flipv { get; set; }
    public string model { get; set; }
    public Textures textures { get; set; }
    public AdditionalModel[] add { get; set; }
}

public class Textures
{
    public string particle { get; set; }
}

public class AdditionalModel
{
    public string model { get; set; }
    public float[] rotation { get; set; }
    public float[] offset { get; set; }
    public bool inheritable { get; set; } = true;
}
