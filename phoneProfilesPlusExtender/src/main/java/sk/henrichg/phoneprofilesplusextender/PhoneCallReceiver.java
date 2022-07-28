package sk.henrichg.phoneprofilesplusextender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Date;

public class PhoneCallReceiver extends BroadcastReceiver {

//    private static TelephonyManager telephony;
    //The receiver will be recreated whenever android feels like it.
    //We need a static variable to remember data between instantiations
//    private static PhoneCallStartEndDetector listener;
    //String outgoingSavedNumber;
//    Context savedContext;

    @Override
    public void onReceive(Context context, Intent intent) {
//        savedContext = context.getApplicationContext();
        
//        if (telephony == null)
//            telephony = (TelephonyManager)savedContext.getSystemService(Context.TELEPHONY_SERVICE);

//        if(listener == null){
//            listener = new PhoneCallStartEndDetector();
//        }

        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
//            PPPEApplication.logE("PhoneCallReceiver.onReceive", "received broadcast action="+intent.getAction());

            /*if (intent.getExtras() != null)
                listener.setOutgoingNumber(intent.getExtras().getString(Intent.EXTRA_PHONE_NUMBER));
            else
                listener.setOutgoingNumber("");*/
            if (intent.getExtras() != null)
                setOutgoingNumber(intent.getExtras().getString(Intent.EXTRA_PHONE_NUMBER));
            else
                setOutgoingNumber("");
//            return;
        }
        /*else {
            if (intent != null) {
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                PPPEApplication.logE("PhoneCallReceiver.onReceive", "incomingNumber=" + incomingNumber);
            }
        }*/

//        listener.onCallStateChanged(intent);
    }

    void setOutgoingNumber(String number){
//        PPPEApplication.logE("PhoneCallReceiver.setOutgoingNumber", "outgoingNumber="+number);
        try {
            if (PPPEApplication.phoneStateListenerSIM1 != null)
                PPPEApplication.phoneStateListenerSIM1.onOutgoingCallStarted(number, new Date());
            if (PPPEApplication.phoneStateListenerSIM2 != null)
                PPPEApplication.phoneStateListenerSIM2.onOutgoingCallStarted(number, new Date());
            if (PPPEApplication.phoneStateListenerDefault != null)
                PPPEApplication.phoneStateListenerDefault.onOutgoingCallStarted(number, new Date());
        } catch (Exception ignored) {}
    }


    //Derived classes should override these to respond to specific events of interest
    //protected abstract void onIncomingCallStarted(String number, Date eventTime);
    //protected abstract void onOutgoingCallStarted(String number, Date eventTime);
    //protected abstract void onOutgoingCallAnswered(String number, Date eventTime);
    //protected abstract void onIncomingCallAnswered(String number, Date eventTime);
    //protected abstract void onIncomingCallEnded(String number, Date eventTime);
    //protected abstract void onOutgoingCallEnded(String number, Date eventTime);
    //protected abstract void onMissedCall(String number, Date eventTime);

/*
    //Deals with actual events
    private class PhoneCallStartEndDetector {
        int lastState = TelephonyManager.CALL_STATE_IDLE;
        Date eventTime;
        boolean inCall;
        boolean isIncoming;
        String savedNumber;  //because the passed incoming is only valid in ringing

        PhoneCallStartEndDetector() {}

        //The outgoing number is only sent via a separate intent, so we need to store it out of band
        void setOutgoingNumber(String number){
            //PPPEApplication.logE("PhoneCallReceiver.PhoneCallStartEndDetector", "outgoingNumber="+number);
            inCall = false;
            isIncoming = false;
            savedNumber = number;
            eventTime = new Date();
            onOutgoingCallStarted(savedNumber, eventTime);
        }

        //Incoming call-  goes from IDLE to RINGING when it rings, to OFF HOOK when it's answered, to IDLE when its hung up
        //Outgoing call-  goes from IDLE to OFF HOOK when it dials out, to IDLE when hung up
        void onCallStateChanged(Intent intent) {
            int state = telephony.getCallState();
            if(lastState == state){
                //No change, de-bounce extras
                return;
            }
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    PPPEApplication.logE("PhoneCallReceiver.PhoneCallStartEndDetector", "state=CALL_STATE_RINGING");
                    String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    //PPPEApplication.logE("PhoneCallReceiver.PhoneCallStartEndDetector", "incomingNumber="+incomingNumber);
                    if ((savedNumber == null) && (incomingNumber == null)) {
                        // CALL_STATE_RINGING is called twice.
                        // When savedNumber and incomingNumber are not filled,
                        // wait for second CALL_STATE_RINGING call.
                        return;
                    }
                    else {
                        inCall = false;
                        isIncoming = true;
                        eventTime = new Date();
                        savedNumber = incomingNumber;
                        //onIncomingCallStarted(incomingNumber, eventTime);
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    PPPEApplication.logE("PhoneCallReceiver.PhoneCallStartEndDetector", "state=CALL_STATE_OFFHOOK");
                    //Transition of ringing->off hook are pickups of incoming calls.  Nothing down on them
                    if(lastState != TelephonyManager.CALL_STATE_RINGING){
                        inCall = true;
                        isIncoming = false;
                        eventTime = new Date();
                        onOutgoingCallAnswered(savedNumber, eventTime);
                    }
                    else
                    {
                        inCall = true;
                        isIncoming = true;
                        eventTime = new Date();
                        //onIncomingCallAnswered(savedNumber, eventTime);
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    PPPEApplication.logE("PhoneCallReceiver.PhoneCallStartEndDetector", "state=CALL_STATE_IDLE");
                    //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                    if(!inCall){
                        //Ring but no pickup-  a miss
                        eventTime = new Date();
                        onMissedCall(savedNumber, eventTime);
                    }
                    else 
                    {
                        //if(isIncoming){
                        //    onIncomingCallEnded(savedNumber, eventTime);
                        //}
                        //else {
                            onOutgoingCallEnded(savedNumber, eventTime);
                        //}
                        inCall = false;
                    }
                    savedNumber = null;
                    break;
            }
            lastState = state;
        }

    }
*/
}
