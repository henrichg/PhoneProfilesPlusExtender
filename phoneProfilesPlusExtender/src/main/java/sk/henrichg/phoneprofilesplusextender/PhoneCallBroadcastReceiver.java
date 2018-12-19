package sk.henrichg.phoneprofilesplusextender;

import android.content.Context;

import java.util.Date;

public class PhoneCallBroadcastReceiver extends PhoneCallReceiver {

    private static final int SERVICE_PHONE_EVENT_START = 1;
    private static final int SERVICE_PHONE_EVENT_ANSWER = 2;
    private static final int SERVICE_PHONE_EVENT_END = 3;

    //static final int CALL_EVENT_UNDEFINED = 0;
    static final int CALL_EVENT_INCOMING_CALL_RINGING = 1;
    //static final int CALL_EVENT_OUTGOING_CALL_STARTED = 2;
    static final int CALL_EVENT_INCOMING_CALL_ANSWERED = 3;
    static final int CALL_EVENT_OUTGOING_CALL_ANSWERED = 4;
    static final int CALL_EVENT_INCOMING_CALL_ENDED = 5;
    static final int CALL_EVENT_OUTGOING_CALL_ENDED = 6;
    static final int CALL_EVENT_MISSED_CALL = 7;

    static final String PREF_CALL_EVENT_TYPE = "callEventType";
    static final String PREF_PHONE_NUMBER = "phoneNumber";
    static final String PREF_EVENT_TIME = "eventTime";

    protected void onIncomingCallStarted(String number, Date eventTime)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_START, true, false, number, eventTime);
    }

    protected void onOutgoingCallStarted(String number, Date eventTime)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_START, false, false, number, eventTime);
    }

    protected void onIncomingCallAnswered(String number, Date eventTime)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_ANSWER, true, false, number, eventTime);
    }

    protected void onOutgoingCallAnswered(String number, Date eventTime)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_ANSWER, false, false, number, eventTime);
    }
    
    protected void onIncomingCallEnded(String number, Date eventTime)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_END, true, false, number, eventTime);
    }

    protected void onOutgoingCallEnded(String number, Date eventTime)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_END, false, false, number, eventTime);
    }

    protected void onMissedCall(String number, Date eventTime)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_END, true, true, number, eventTime);
    }

    private void doCall(final Context context, final int phoneEvent,
                            final boolean incoming, final boolean missed,
                            final String number, final Date eventTime) {
        final Context appContext = context.getApplicationContext();
        switch (phoneEvent) {
            case SERVICE_PHONE_EVENT_START:
                callStarted(incoming, number, eventTime, appContext);
                break;
            case SERVICE_PHONE_EVENT_ANSWER:
                callAnswered(incoming, number, eventTime, appContext);
                break;
            case SERVICE_PHONE_EVENT_END:
                callEnded(incoming, missed, number, eventTime, appContext);
                break;
        }
    }

    private static void doCallEvent(int servicePhoneEvent, int eventType, String phoneNumber, Date eventTime, Context context)
    {
        /*
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_EVENT_CALL_EVENT_TYPE, eventType);
        editor.putString(PREF_EVENT_CALL_PHONE_NUMBER, phoneNumber);
        editor.putLong(PREF_EVENT_CALL_EVENT_TIME, eventTime.getTime());
        editor.apply();
        */
    }

    private static void callStarted(boolean incoming, String phoneNumber, Date eventTime, Context context)
    {
        PPPEApplication.logE("PhoneCallBroadcastReceiver.callStarted", "incoming="+incoming);
        PPPEApplication.logE("PhoneCallBroadcastReceiver.callStarted", "phoneNumber="+phoneNumber);

        if (incoming) {
            doCallEvent(SERVICE_PHONE_EVENT_START, CALL_EVENT_INCOMING_CALL_RINGING, phoneNumber, eventTime, context);
        }
    }

    private static void callAnswered(boolean incoming, String phoneNumber, Date eventTime, Context context)
    {
        if (incoming)
            doCallEvent(SERVICE_PHONE_EVENT_ANSWER, CALL_EVENT_INCOMING_CALL_ANSWERED, phoneNumber, eventTime, context);
        else
            doCallEvent(SERVICE_PHONE_EVENT_ANSWER, CALL_EVENT_OUTGOING_CALL_ANSWERED, phoneNumber, eventTime, context);
    }

    private static void callEnded(boolean incoming, boolean missed, String phoneNumber, Date eventTime, Context context)
    {
        PPPEApplication.logE("PhoneCallBroadcastReceiver.callEnded", "incoming="+incoming);
        PPPEApplication.logE("PhoneCallBroadcastReceiver.callEnded", "missed="+missed);
        PPPEApplication.logE("PhoneCallBroadcastReceiver.callEnded", "phoneNumber="+phoneNumber);

        if (incoming) {
            if (missed)
                doCallEvent(SERVICE_PHONE_EVENT_END, CALL_EVENT_MISSED_CALL, phoneNumber, eventTime, context);
            else
                doCallEvent(SERVICE_PHONE_EVENT_END, CALL_EVENT_INCOMING_CALL_ENDED, phoneNumber, eventTime, context);
        }
        else
            doCallEvent(SERVICE_PHONE_EVENT_END, CALL_EVENT_OUTGOING_CALL_ENDED, phoneNumber, eventTime, context);

    }

}
