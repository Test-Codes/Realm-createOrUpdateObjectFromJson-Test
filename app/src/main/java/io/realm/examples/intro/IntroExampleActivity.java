/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.examples.intro;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;


public class IntroExampleActivity extends Activity {

    private ContentResolver contentResolver;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getInstance(this);
        contentResolver = getContentResolver();

        clearDatabase();
        saveContactsToRealm();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private void clearDatabase() {
        realm.beginTransaction();
        realm.allObjects(Contact.class).clear();
        realm.commitTransaction();
    }

    /**
     * Contact
     */
    private Uri QUERY_URI = ContactsContract.Contacts.CONTENT_URI;
    private String CONTACT_ID = ContactsContract.Contacts._ID;
    private String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
    private Uri EMAIL_CONTENT_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
    private String EMAIL_CONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
    private String EMAIL_DATA = ContactsContract.CommonDataKinds.Email.DATA;
    private String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
    private String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    private Uri PHONE_CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    private String PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
    private String STARRED_CONTACT = ContactsContract.Contacts.STARRED;

    public void saveContactsToRealm() {
        Logger.d("Started : " + System.currentTimeMillis());
        List<Contact> contacts = new ArrayList<>();
        String[] projection = new String[]{CONTACT_ID, DISPLAY_NAME, HAS_PHONE_NUMBER, STARRED_CONTACT};
        String selection = null;
        Cursor cursor = contentResolver.query(QUERY_URI, projection, selection, null, null);

        while (cursor.moveToNext())
            contacts.add(getContact(cursor));
        Logger.d("Finished #1 : " + System.currentTimeMillis() + ", Size : " + contacts.size());

//        Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
//            @Override
//            public boolean shouldSkipField(FieldAttributes f) {
//                return f.getDeclaringClass().equals(RealmObject.class);
//            }
//
//            @Override
//            public boolean shouldSkipClass(Class<?> clazz) {
//                return false;
//            }
//        }).create();
//        String contactsJson = gson.toJsonTree(contacts).getAsJsonArray().toString();
//        Logger.d("ContactsJson : " + contactsJson);
//        Logger.d("JsonCreated : " + System.currentTimeMillis());
//
//        realm.beginTransaction();
//        realm.createOrUpdateAllFromJson(Contact.class, contactsJson);
//        realm.commitTransaction();

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(contacts);
        realm.commitTransaction();

        realm.beginTransaction();
        RealmResults<Contact> contactRealmResults = realm.allObjects(Contact.class);
        Logger.d("Finished : " + System.currentTimeMillis() + ", Size : " + contactRealmResults.size());
        realm.commitTransaction();

        cursor.close();
    }

    private Contact getContact(Cursor cursor) {
        String contactId = cursor.getString(cursor.getColumnIndex(CONTACT_ID));
        String name = (cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)));
        Uri uri = Uri.withAppendedPath(QUERY_URI, String.valueOf(contactId));

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        String intentUriString = intent.toUri(0);

        Contact contact = new Contact();
        contact.setId(Integer.valueOf(contactId));
        contact.setName(name);
        contact.setUriString(intentUriString);
        getPhone(cursor, contactId, contact);
        getEmail(contactId, contact);

        return contact;
    }

    private void getEmail(String contactId, Contact contact) {
        Cursor emailCursor = contentResolver.query(EMAIL_CONTENT_URI, null, EMAIL_CONTACT_ID + " = ?", new String[]{contactId}, null);
        while (emailCursor.moveToNext()) {
            String email = emailCursor.getString(emailCursor.getColumnIndex(EMAIL_DATA));
            if (!TextUtils.isEmpty(email)) {
                contact.setEmail(email);
            }
        }
        emailCursor.close();
    }

    private void getPhone(Cursor cursor, String contactId, Contact contact) {
        int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));
        if (hasPhoneNumber > 0) {
            Cursor phoneCursor = contentResolver.query(PHONE_CONTENT_URI, null, PHONE_CONTACT_ID + " = ?", new String[]{contactId}, null);
            while (phoneCursor.moveToNext()) {
                String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(PHONE_NUMBER));
                contact.setPhone(phoneNumber);
            }
            phoneCursor.close();
        }
    }
}
