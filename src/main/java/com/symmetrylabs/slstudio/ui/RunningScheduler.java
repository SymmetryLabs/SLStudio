//
//
//import java.util.Calendar;
//
//
//public class RunningScheduler extends LXRunnableComponent {
//
//    private final int MINUTES_PER_HOUR = 60;
//
//    public final DiscreteParameter startHour = new DiscreteParameter("startHour", 19, 1, 24);
//    public final DiscreteParameter startMinute = new DiscreteParameter("startMinute", 0, 0, 59);
//
//    public final DiscreteParameter stopHour = new DiscreteParameter("stopHour", 19, 1, 24);
//    public final DiscreteParameter stopMinute = new DiscreteParameter("stopMinute", 0, 0, 59);
//
//    public RunningScheduler(LX lx) {
//        super(lx);
//        addParameter(startHour);
//        addParameter(startMinute);
//        addParameter(endHour);
//        addParameter(endMinute);
//
//        startHour.addListener(this::checkBounds);
//        startMinutes.addListener(this::checkBounds);
//        endHour.addListener(this::checkBounds);
//        endMinutes.addListener(this::checkBounds);
//    }
//
//    private void checkBounds() {
//        int startDayMinutes = getStartTime();
//        int endDayMinutes = getStopTime;
//
//        if (startDayMinutes > endDayMinutes) {
//            startHour.setValue(endDayMinutes / 60);
//            startDay.setValue((endDayMinutes % 60) - 1);
//        }
//    }
//
//    public void run(double deltaMs) {
//        int currentTime = getCurrentMinutesElapsed();
//        int startTime = getStartTime();
//        int stopTime = getStopTime();
//
//        if (currentTime > startTime && )
//    }
//
//    private int getCurrentMinutesElapsed() {
//        Calendar calendar = Calendar.getInstance();
//        int minutes = calendar.get(Calendar.MINUTE);
//        int hours = calendar.get(Calendar.HOUR_OF_DAY);
//        return hours * MINUTES_PER_HOUR + minutes;
//    }
//
//    private int getStartTime() {
//        return startHour.getValuei() * MINUTES_PER_HOUR + startMinutes.getValuei();
//    }
//
//    private int getStopTime() {
//        return stopHour.getValuei() * MINUTES_PER_HOUR + stopMinutes.getValuei()
//    }
//}
