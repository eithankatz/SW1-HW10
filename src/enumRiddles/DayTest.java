package enumRiddles;

enum Day {
	   MONDAY,
	   TUESDAY,
	   WEDNESDAY,
	   THURSDAY,
	   FRIDAY,
	   SATURDAY,
	   SUNDAY;
	 
	   public Day next()
	   {
		   return Day.values()[this.getDayNumber() % 7];
		   }
	 
	   int getDayNumber() 
	   {
	      return this.ordinal() + 1;
	   }
	}
	   
	public class DayTest {
	   public static void main(String[] args) {
	      for (Day day : Day.values()) {
	         System.out.printf("%s (%d), next is %s\n", day, day.getDayNumber(), day.next());
	      }
	   }
	}
