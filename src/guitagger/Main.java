package guitagger;

import org.jaudiotagger.tag.reference.GenreTypes;

public class Main
{
   public Main()
   {
	   Gui gui = new Gui();
   }
   public static void main(String [] args)
   {
	  System.out.println(GenreTypes.getInstanceOf().getValueToIdMap().keySet());
      Main m = new Main();
   }
}