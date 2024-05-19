package sk.henrichg.phoneprofilesplusextender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import java.util.Calendar;

@SuppressWarnings("SpellCheckingInspection")
public class SMSBroadcastReceiver extends BroadcastReceiver {

    private static final String ACTION_SMS_MMS_RECEIVED = PPPEApplication.PACKAGE_NAME + ".ACTION_SMS_MMS_RECEIVED";
    private static final String EXTRA_ORIGIN = PPPEApplication.PACKAGE_NAME + ".origin";
    private static final String EXTRA_TIME = PPPEApplication.PACKAGE_NAME + ".time";
    private static final String EXTRA_SUBSCRIPTION_ID = PPPEApplication.PACKAGE_NAME + ".subscription_id";

    //private static ContentObserver smsObserver;
    //private static ContentObserver mmsObserver;
    //private static int mmsCount;

    /** @noinspection ExtractMethodRecommender*/
    @Override
    public void onReceive(Context context, Intent intent) {
//        PPPEApplication.logE("##### SMSBroadcastReceiver.onReceive", "xxx");

        //if (intent != null)
        //    PPPEApplication.logE("SMSBroadcastReceiver.onReceive","intent.getAction()="+intent.getAction());

        PPPEApplication.logE("[MEMORY_LEAK] SMSBroadcastReceiver.onReceive", "xxxx");

        boolean smsMmsReceived = false;

        String origin = "";
        //String body = "";
        int subscriptionId = -1;

        //String smsAction = "android.provider.Telephony.SMS_RECEIVED";
        //String mmsAction = "android.provider.Telephony.WAP_PUSH_RECEIVED";
        String smsAction = Telephony.Sms.Intents.SMS_RECEIVED_ACTION;
        String mmsAction = Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION;

        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(smsAction))
        {
//            PPPEApplication.logE("SMSBroadcastReceiver.onReceive","SMS received");

            smsMmsReceived = true;

            Bundle extras = intent.getExtras();
            if (extras != null) {

                // from: https://github.com/moezbhatti/qksms/blob/master/data/src/main/java/com/moez/QKSMS/receiver/SmsReceiver.kt
                subscriptionId = extras.getInt("subscription", -1);
//                PPPEApplication.logE("SMSBroadcastReceiver.onReceive","subscriptionId="+subscriptionId);

                Object[] pdus = (Object[]) extras.get("pdus");
                if (pdus != null) {
                    String format = (String) extras.get("format");
                    for (Object pdu : pdus) {
                        SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdu, format);
                        if (msg != null) {
                            origin = msg.getOriginatingAddress();
                            //body = msg.getMessageBody();
                        }
                    }
                }
            }
        }
        /*else
        if(intent.getAction().equals("android.provider.Telephony.SMS_SENT"))
        {
            PPPEApplication.logE("SMSBroadcastReceiver.onReceive","sent");
        }*/
        else
        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(mmsAction)) {
            String type = intent.getType();

            //PPPEApplication.logE("SMSBroadcastReceiver.onReceive", "MMS received");
            //PPPEApplication.logE("SMSBroadcastReceiver.onReceive", "type="+type);

            if ((type != null) && type.equals("application/vnd.wap.mms-message")) {

                smsMmsReceived = true;

                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    byte[] buffer = bundle.getByteArray("data");
                    if (buffer != null) {
                        String incomingNumber = new String(buffer);
                        int idx = incomingNumber.indexOf("/TYPE");

                        if (idx > 0 && (idx - 15) > 0) {
                            int newIdx = idx - 15;
                            incomingNumber = incomingNumber.substring(newIdx, idx);
                            idx = incomingNumber.indexOf("+");
                            if (idx > 0) {
                                origin = incomingNumber.substring(idx);
                            }
                        }
                    }
                /*int transactionId = bundle.getInt("transactionId");
                int pduType = bundle.getInt("pduType");
                byte[] buffer2 = bundle.getByteArray("header");      
                String header = new String(buffer2);*/
                }
            }
        }

        //PPPEApplication.logE("@@@ SMSBroadcastReceiver.onReceive","smsMmsReceived="+smsMmsReceived);

        if (smsMmsReceived)
        {
            //PPPEApplication.logE("SMSBroadcastReceiver.onReceive","from="+origin);

            if (PPPEApplication.registeredSMSFunctionPPP) {
                Calendar now = Calendar.getInstance();
                int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                long time = now.getTimeInMillis() + gmtOffset;

//                PPPEApplication.logE("[BROADCAST_TO_PPP] SMSBroadcastReceiver.onReceive", "xxxx");
                Intent sendIntent = new Intent(ACTION_SMS_MMS_RECEIVED);
                sendIntent.putExtra(EXTRA_ORIGIN, origin);  //TODO encrypt it!!!
                sendIntent.putExtra(EXTRA_TIME, time);
                sendIntent.putExtra(EXTRA_SUBSCRIPTION_ID, subscriptionId);
                context.getApplicationContext().sendBroadcast(sendIntent, PPPEApplication.ACCESSIBILITY_SERVICE_PERMISSION);
            }
        }
    }

/*
    private static final String CONTENT_SMS = "content://sms";
    // Constant from Android SDK
    private static final int MESSAGE_TYPE_SENT = 2;


    // Register an observer for listening outgoing sms events.
    // @author khoanguyen
    static public void registerSMSContentObserver(Context context)
    {
        if (smsObserver != null)
            return;

        final Context _context = context;

        smsObserver = new ContentObserver(null)
        {
            public void onChange(boolean selfChange)
            {
                PPPEApplication.logE("SMSBroadcastReceiver.smsObserver.onChange","xxx");

                // read outgoing sms from db
                Cursor cursor = _context.getContentResolver().query(Uri.parse(CONTENT_SMS), null, null, null, null);
                if (cursor.moveToNext())
                {
                    String protocol = cursor.getString(cursor.getColumnIndex("protocol"));
                    int type = cursor.getInt(cursor.getColumnIndex("type"));
                    // Only processing outgoing sms event & only when it
                    // is sent successfully (available in SENT box).
                    if (protocol != null || type != MESSAGE_TYPE_SENT)
                    {
                        PPPEApplication.logE("SMSBroadcastReceiver.smsObserver.onChange","no SMS in SENT box");
                        return;
                    }
                    int dateColumn = cursor.getColumnIndex("date");
                    //int bodyColumn = cursor.getColumnIndex("body");
                    int addressColumn = cursor.getColumnIndex("address");

                    String to = cursor.getString(addressColumn);
                    Date date = new Date(cursor.getLong(dateColumn));
                    //String message = cursor.getString(bodyColumn);

                    PPPEApplication.logE("SMSBroadcastReceiver.smsObserver.onChange","sms sent");
                    PPPEApplication.logE("SMSBroadcastReceiver.smsObserver.onChange","to="+to);
                    PPPEApplication.logE("SMSBroadcastReceiver.smsObserver.onChange","date="+date);
                    //PPPEApplication.logE("SMSBroadcastReceiver.smsObserver.onChange","message="+message);

                    SharedPreferences preferences = _context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
                    Editor editor = preferences.edit();
                    editor.putInt(PPApplication.PREF_EVENT_SMS_EVENT_TYPE, EventPreferencesSMS.SMS_EVENT_OUTGOING);
                    editor.putString(PPApplication.PREF_EVENT_SMS_PHONE_NUMBER, to);
                    int gmtOffset = TimeZone.getDefault().getRawOffset();
                    long time = date.getTime() + gmtOffset;
                    editor.putLong(PPApplication.PREF_EVENT_SMS_DATE, time);
                    editor.commit();
                }
                cursor.close();

                startService(_context);
            }
        };

        context.getContentResolver().registerContentObserver(Uri.parse(CONTENT_SMS), true, smsObserver);
    }

    public static void unregisterSMSContentObserver(Context context)
    {
        if (smsObserver != null)
            context.getContentResolver().unregisterContentObserver(smsObserver);
    }

    // not working with with Hangouts :-/
    static public void registerMMSContentObserver(Context context)
    {
        if (mmsObserver != null)
            return;

        Uri uriMMS = Uri.parse("content://mms");
        Cursor mmsCur = context.getContentResolver().query(uriMMS, null, "msg_box = 4", null, "_id");
        if (mmsCur != null && mmsCur.getCount() > 0) {
           mmsCount = mmsCur.getCount();
        }

        final Context _context = context;

        mmsObserver = new ContentObserver(null)
        {
            public void onChange(boolean selfChange)
            {
                PPPEApplication.logE("SMSBroadcastReceiver.mmsObserver.onChange","xxx");

                // read outgoing mms from db
                Uri uriMMS = Uri.parse("content://mms/");
                Cursor mmsCur = _context.getContentResolver().query(uriMMS, null, "msg_box = 4 or msg_box = 1", null,"_id");

                int currMMSCount = 0;
                if (mmsCur != null && mmsCur.getCount() > 0) {
                   currMMSCount = mmsCur.getCount();
                }

                PPPEApplication.logE("SMSBroadcastReceiver.mmsObserver.onChange","mmsCount="+mmsCount);
                PPPEApplication.logE("SMSBroadcastReceiver.mmsObserver.onChange","currMMSCount="+currMMSCount);
                
                if (currMMSCount > mmsCount)
                {
                    if (mmsCur.moveToLast())
                    {
                        // 132 (RETRIEVE CONF) 130 (NOTIF IND) 128 (SEND REQ)
                        int type = Integer.parseInt(mmsCur.getString(mmsCur.getColumnIndex("m_type")));

                        PPPEApplication.logE("SMSBroadcastReceiver.mmsObserver.onChange","type="+type);

                        if (type == 128) {
                           // Outgoing MMS

                            PPPEApplication.logE("SMSBroadcastReceiver.mmsObserver.onChange","mms sent");

                            int id = Integer.parseInt(mmsCur.getString(mmsCur.getColumnIndex("_id")));

                            // Get Address
                            Uri uriAddrPart = Uri.parse("content://mms/"+id+"/addr");
                            Cursor addrCur = _context.getContentResolver().query(uriAddrPart, null, "type=151", null, "_id");
                            if (addrCur != null)
                            {
                                if (addrCur.moveToLast())
                                {
                                    do
                                    {
                                        int addColIndex = addrCur.getColumnIndex("address");
                                         String to = addrCur.getString(addColIndex);

                                        PPPEApplication.logE("SMSBroadcastReceiver.mmsObserver.onChange","to="+to);

                                        SharedPreferences preferences = _context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
                                        Editor editor = preferences.edit();
                                        editor.putInt(PPApplication.PREF_EVENT_SMS_EVENT_TYPE, EventPreferencesSMS.SMS_EVENT_OUTGOING);
                                        editor.putString(PPApplication.PREF_EVENT_SMS_PHONE_NUMBER, to);
                                        Calendar now = Calendar.getInstance();
                                        int gmtOffset = TimeZone.getDefault().getRawOffset();
                                        long time = now.getTimeInMillis() + gmtOffset;
                                        editor.putLong(PPApplication.PREF_EVENT_SMS_DATE, time);
                                        editor.commit();

                                        startService(_context);
                                    }
                                    while (addrCur.moveToPrevious());
                                }
                            }
                        }
                    }
                }
                
                mmsCount = currMMSCount;
                
            }
        };

        context.getContentResolver().registerContentObserver(Uri.parse("content://mms-sms"), true, mmsObserver);
    }

    public static void unregisterMMSContentObserver(Context context)
    {
        if (mmsObserver != null)
            context.getContentResolver().unregisterContentObserver(mmsObserver);
    }
*/
}
