package sk.henrichg.phoneprofilesplusextender;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;

import java.util.Date;

public class PPPEPhoneStateListener extends PhoneStateListener {

    final SubscriptionInfo subscriptionInfo;

    final Context savedContext;

    int lastState = TelephonyManager.CALL_STATE_IDLE;
    Date eventTime;
    boolean inCall;
    boolean isIncoming;
    String savedNumber;  //because the passed incoming is only valid in ringing

    static final String ACTION_CALL_RECEIVED = PPPEApplication.PACKAGE_NAME + ".ACTION_CALL_RECEIVED";
    //private static final String EXTRA_SERVICE_PHONE_EVENT = PPPEApplication.PACKAGE_NAME + ".service_phone_event";
    static final String EXTRA_CALL_EVENT_TYPE = PPPEApplication.PACKAGE_NAME + ".call_event_type";
    static final String EXTRA_PHONE_NUMBER = PPPEApplication.PACKAGE_NAME + ".phone_number";
    static final String EXTRA_EVENT_TIME = PPPEApplication.PACKAGE_NAME + ".event_time";
    static final String EXTRA_SIM_SLOT = PPPEApplication.PACKAGE_NAME + ".sim_slot";

    static final int SERVICE_PHONE_EVENT_START = 1;
    static final int SERVICE_PHONE_EVENT_ANSWER = 2;
    static final int SERVICE_PHONE_EVENT_END = 3;

    //static final int CALL_EVENT_UNDEFINED = 0;
    static final int CALL_EVENT_INCOMING_CALL_RINGING = 1;
    //static final int CALL_EVENT_OUTGOING_CALL_STARTED = 2;
    static final int CALL_EVENT_INCOMING_CALL_ANSWERED = 3;
    static final int CALL_EVENT_OUTGOING_CALL_ANSWERED = 4;
    static final int CALL_EVENT_INCOMING_CALL_ENDED = 5;
    static final int CALL_EVENT_OUTGOING_CALL_ENDED = 6;
    static final int CALL_EVENT_MISSED_CALL = 7;
    static final int CALL_EVENT_SERVICE_UNBIND = 8;

    PPPEPhoneStateListener(SubscriptionInfo subscriptionInfo, Context context) {
        this.subscriptionInfo = subscriptionInfo;
        this.savedContext = context.getApplicationContext();
    }

    @SuppressWarnings("deprecation")
    public void onCallStateChanged (int state, String phoneNumber) {

        if(lastState == state){
            //No change, de-bounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
//                PPPEApplication.logE("PPPEPhoneStateListener.onCallStateChanged", "state=CALL_STATE_RINGING");
//                if (subscriptionInfo != null)
//                    PPPEApplication.logE("PPPEPhoneStateListener.onCallStateChanged", "simSlot="+subscriptionInfo.getSimSlotIndex());
//                else
//                    PPPEApplication.logE("PPPEPhoneStateListener.onCallStateChanged", "simSlot=0");

//                if (phoneNumber == null)
//                    PPPEApplication.logE("PPPEPhoneStateListener.onCallStateChanged", "phoneNumber=null");
//                else
//                    PPPEApplication.logE("PPPEPhoneStateListener.onCallStateChanged", "phoneNumber="+phoneNumber);
//                if (savedNumber == null)
//                    PPPEApplication.logE("PPPEPhoneStateListener.onCallStateChanged", "savedNumber=null");
//                else
//                    PPPEApplication.logE("PPPEPhoneStateListener.onCallStateChanged", "savedNumber="+savedNumber);
                if ((savedNumber == null) && (phoneNumber == null)) {
                    // CALL_STATE_RINGING is called twice.
                    // When savedNumber and incomingNumber are not filled,
                    // wait for second CALL_STATE_RINGING call.
                    return;
                }
                else {
                    inCall = false;
                    isIncoming = true;
                    eventTime = new Date();
                    savedNumber = phoneNumber;
                    onIncomingCallStarted(phoneNumber, eventTime);
                }
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
//                PPPEApplication.logE("PPPEPhoneStateListener.onCallStateChanged", "state=CALL_STATE_OFFHOOK");
//                if (subscriptionInfo != null)
//                    PPPEApplication.logE("PPPEPhoneStateListener.onCallStateChanged", "simSlot="+subscriptionInfo.getSimSlotIndex());
//                else
//                    PPPEApplication.logE("PPPEPhoneStateListener.onCallStateChanged", "simSlot=0");
                //Transition of ringing->off hook are pickups of incoming calls.  Nothing down on them

//                PPPEApplication.logE("PPPEPhoneStateListener.onCallStateChanged", "incomingNumber="+phoneNumber);
                if(lastState != TelephonyManager.CALL_STATE_RINGING){
//                    PPPEApplication.logE("PPPEPhoneStateListener.onCallStateChanged", "isIncoming=false");
//                    PPPEApplication.logE("PPPEPhoneStateListener.onCallStateChanged", "savedNumber="+savedNumber);
                    inCall = true;
                    isIncoming = false;
                    eventTime = new Date();
                    onOutgoingCallAnswered(savedNumber, eventTime);
                }
                else {
//                    PPPEApplication.logE("PPPEPhoneStateListener.onCallStateChanged", "isIncoming=true");
                    inCall = true;
                    isIncoming = true;
                    eventTime = new Date();
                    onIncomingCallAnswered(savedNumber, eventTime);
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
//                PPPEApplication.logE("PPPEPhoneStateListener.onCallStateChanged", "state=CALL_STATE_IDLE");
//                if (subscriptionInfo != null)
//                    PPPEApplication.logE("PPPEPhoneStateListener.onCallStateChanged", "simSlot="+subscriptionInfo.getSimSlotIndex());
//                else
//                    PPPEApplication.logE("PPPEPhoneStateListener.onCallStateChanged", "simSlot=0");
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if(!inCall){
                    //Ring but no pickup-  a miss
                    eventTime = new Date();
                    onMissedCall(savedNumber, eventTime);
                }
                else
                {
                    if(isIncoming){
                        onIncomingCallEnded(savedNumber, eventTime);
                    }
                    else {
                        onOutgoingCallEnded(savedNumber, eventTime);
                    }
                    inCall = false;
                }
                savedNumber = null;
                break;
        }
        lastState = state;
    }

    protected void onIncomingCallStarted(String number, Date eventTime)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_START, true, false, number, eventTime);
    }

    protected void onIncomingCallAnswered(String number, Date eventTime)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_ANSWER, true, false, number, eventTime);
    }

    protected void onIncomingCallEnded(String number, Date eventTime)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_END, true, false, number, eventTime);
    }

    protected void onMissedCall(String number, Date eventTime)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_END, true, true, number, eventTime);
    }

    protected void onOutgoingCallStarted(String number, Date _eventTime)
    {
//        PPPEApplication.logE("PPPEPhoneStateListener.onOutgoingCallStarted", "number="+number);
        savedNumber=number;
        eventTime = _eventTime;
        doCall(savedContext, SERVICE_PHONE_EVENT_START, false, false, number, eventTime);
    }

    protected void onOutgoingCallAnswered(String number, Date eventTime)
    {
//        PPPEApplication.logE("PPPEPhoneStateListener.onOutgoingCallAnswered", "number="+number);
        doCall(savedContext, SERVICE_PHONE_EVENT_ANSWER, false, false, number, eventTime);
    }

    protected void onOutgoingCallEnded(String number, Date eventTime)
    {
        doCall(savedContext, SERVICE_PHONE_EVENT_END, false, false, number, eventTime);
    }

    private void doCall(final Context context, final int phoneEvent,
                        final boolean incoming, final boolean missed,
                        final String number, final Date eventTime) {
        final Context appContext = context.getApplicationContext();


        int simSlot = 0;
        if (subscriptionInfo != null)
            simSlot = subscriptionInfo.getSimSlotIndex()+1;

        switch (phoneEvent) {
            case SERVICE_PHONE_EVENT_START:
                callStarted(incoming, number, eventTime, appContext, simSlot);
                break;
            case SERVICE_PHONE_EVENT_ANSWER:
                callAnswered(incoming, number, eventTime, appContext, simSlot);
                break;
            case SERVICE_PHONE_EVENT_END:
                callEnded(incoming, missed, number, eventTime, appContext, simSlot);
                break;
        }
    }

    private static void doCallEvent(/*int servicePhoneEvent, */int eventType, String phoneNumber, Date eventTime, Context context, int simSlot)
    {
        //PPPEApplication.logE("PPPEPhoneStateListener.doCallEvent", "PPPEApplication.registeredCallFunctionPPP="+PPPEApplication.registeredCallFunctionPPP);
        if (PPPEApplication.registeredCallFunctionPPP) {
//            PPPEApplication.logE("[BROADCAST_TO_PPP] PPPEPhoneStateListener.doCallEvent", "xxxx");
            Intent sendIntent = new Intent(ACTION_CALL_RECEIVED);
            //sendIntent.putExtra(EXTRA_SERVICE_PHONE_EVENT, servicePhoneEvent);
            sendIntent.putExtra(EXTRA_CALL_EVENT_TYPE, eventType);
            sendIntent.putExtra(EXTRA_PHONE_NUMBER, phoneNumber); //TODO encrypt it!!!
            sendIntent.putExtra(EXTRA_EVENT_TIME, eventTime.getTime());
            sendIntent.putExtra(EXTRA_SIM_SLOT, simSlot);
            context.sendBroadcast(sendIntent, PPPEApplication.ACCESSIBILITY_SERVICE_PERMISSION);
        }
    }

    private static void callStarted(boolean incoming, String phoneNumber, Date eventTime, Context context, int simSlot)
    {
//        PPPEApplication.logE("PPPEPhoneStateListener.callStarted", "incoming="+incoming);
//        PPPEApplication.logE("PPPEPhoneStateListener.callStarted", "phoneNumber="+phoneNumber);

//        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
//        audioManager.setSpeakerphoneOn(true);

        if (incoming) {
            doCallEvent(/*SERVICE_PHONE_EVENT_START, */CALL_EVENT_INCOMING_CALL_RINGING, phoneNumber, eventTime, context, simSlot);
        }
    }

    private static void callAnswered(boolean incoming, String phoneNumber, Date eventTime, Context context, int simSlot)
    {
//        PPPEApplication.logE("PPPEPhoneStateListener.callAnswered", "incoming="+incoming);
//        PPPEApplication.logE("PPPEPhoneStateListener.callAnswered", "phoneNumber="+phoneNumber);

//        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
//        audioManager.setSpeakerphoneOn(true);

        if (incoming)
            doCallEvent(/*SERVICE_PHONE_EVENT_ANSWER, */CALL_EVENT_INCOMING_CALL_ANSWERED, phoneNumber, eventTime, context, simSlot);
        else
            doCallEvent(/*SERVICE_PHONE_EVENT_ANSWER, */CALL_EVENT_OUTGOING_CALL_ANSWERED, phoneNumber, eventTime, context, simSlot);
    }

    private static void callEnded(boolean incoming, boolean missed, String phoneNumber, Date eventTime, Context context, int simSlot)
    {
//        PPPEApplication.logE("PPPEPhoneStateListener.callEnded", "incoming="+incoming);
//        PPPEApplication.logE("PPPEPhoneStateListener.callEnded", "missed="+missed);
//        PPPEApplication.logE("PPPEPhoneStateListener.callEnded", "phoneNumber="+phoneNumber);

        if (incoming) {
            if (missed)
                doCallEvent(/*SERVICE_PHONE_EVENT_END, */CALL_EVENT_MISSED_CALL, phoneNumber, eventTime, context, simSlot);
            else
                doCallEvent(/*SERVICE_PHONE_EVENT_END, */CALL_EVENT_INCOMING_CALL_ENDED, phoneNumber, eventTime, context, simSlot);
        }
        else
            doCallEvent(/*SERVICE_PHONE_EVENT_END, */CALL_EVENT_OUTGOING_CALL_ENDED, phoneNumber, eventTime, context, simSlot);

//        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
//        audioManager.setSpeakerphoneOn(false);

    }

}
