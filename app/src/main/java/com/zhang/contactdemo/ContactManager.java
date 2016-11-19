package com.zhang.contactdemo;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mr.Z on 2016/11/18 0018.
 * <p>
 * ContactsContract defines an extensible database of contact-related information.
 * Contact information is stored in a three-tier data model:
 * <p>
 * A row in the ContactsContract.Data table can store any kind of personal data,
 * such as a phone number or email addresses. The set of data kinds that can be
 * stored in this table is open-ended. There is a predefined set of common kinds,
 * but any application can add its own data kinds.
 * <p>
 * <p>
 * A row in the ContactsContract.RawContacts table represents a set of data describing
 * a person and associated with a single account (for example, one of the user's Gmail
 * accounts).
 * <p>
 * <p>
 * A row in the ContactsContract.Contacts table represents an aggregate of one or more
 * RawContacts presumably describing the same person. When data in or associated with
 * the RawContacts table is changed, the affected aggregate contacts are updated as necessary.
 */

public class ContactManager {


    /**
     * @param context
     * @return
     */
    public static List<ContactBean> getContacts(Context context) {
        List<ContactBean> contacts = new ArrayList<>();

        ContentResolver resolver = context.getContentResolver();

        //先查询出ContactsContract.RawContacts
        Cursor cursor = resolver.query(ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID},
                null,
                null,
                null);

        ContactBean contact;
        //遍历RawContacts
        while (cursor.moveToNext()) {
            contact = new ContactBean();

            //RawContact的id
            long rawContactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.RawContacts._ID));
            contact.setRawContactId(rawContactId);

            //查询出RawContact中的ContactsContract.Data
            Cursor c = resolver.query(ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.RAW_CONTACT_ID + "=?",
                    new String[]{String.valueOf(rawContactId)},
                    null);


            while (c.moveToNext()) {
                String data1 = c.getString(c.getColumnIndex(ContactsContract.Data.DATA1));
                String mimeType = c.getString(c.getColumnIndex(ContactsContract.Data.MIMETYPE));

                if (mimeType.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
                    contact.setName(data1);
                } else if (mimeType.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                    contact.setPhone(data1);
                }
            }
            contacts.add(contact);
            c.close();
        }
        cursor.close();
        return contacts;
    }

    /**
     * @param context
     * @param contact
     */
    public static void addContact(Context context, ContactBean contact) {
        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues();
        Uri rawContactUri = resolver.insert(ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);

        ContentValues valuesData1 = new ContentValues();
        valuesData1.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        valuesData1.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        valuesData1.put(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.getPhone());
        resolver.insert(ContactsContract.Data.CONTENT_URI, valuesData1);

        ContentValues valuesData2 = new ContentValues();
        valuesData2.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        valuesData2.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        valuesData2.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.getName());
        resolver.insert(ContactsContract.Data.CONTENT_URI, valuesData2);
    }

    /**
     * @param context
     * @param contact
     */
    public static void updateContact(Context context, ContactBean contact) {
        ContentResolver resolver = context.getContentResolver();

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation
                .newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                        ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "=?",
                        new String[]{
                                String.valueOf(contact.getRawContactId()),
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE})
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.getName())
                .build());

        ops.add(ContentProviderOperation
                .newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                        ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "=?",
                        new String[]{
                                String.valueOf(contact.getRawContactId()),
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE})
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.getPhone()).build());

        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param context
     * @param contact
     */
    public static void deleteContact(Context context, ContactBean contact) {
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(ContactsContract.RawContacts.CONTENT_URI, ContactsContract.RawContacts._ID + "=?",
                new String[]{String.valueOf(contact.getRawContactId())});
    }
}
