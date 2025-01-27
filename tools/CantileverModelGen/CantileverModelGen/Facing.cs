namespace CantileverModelGen
{
    internal class Facing
    {
        public static readonly Facing NORTH = new Facing() { rotation = 0, name = "north" };
        public static readonly Facing EAST = new Facing() { rotation = 90, name = "east" };
        public static readonly Facing SOUTH = new Facing() { rotation = 180, name = "south" };
        public static readonly Facing WEST = new Facing() { rotation = 270, name = "west" };

        private Facing()
        {
        }

        public int rotation { private set; get; }
        public string name { private set; get; }

        public static Facing[] Values()
        {
            return new Facing[]
            {
                NORTH, EAST, SOUTH, WEST
            };
        }

        public static Facing GetByName(string name)
        {
            return Values().Where(x => x.name == name).FirstOrDefault(NORTH);
        }
    }
}
