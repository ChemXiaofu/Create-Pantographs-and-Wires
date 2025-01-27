
public class Blockstates
{
    public Dictionary<string, Variant> variants { get; set; } = new Dictionary<string, Variant>();
}

public class Variant
{
    public string model { get; set; }
    public int y { get; set; }
}