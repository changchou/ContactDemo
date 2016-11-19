package com.zhang.contactdemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LetterBar lb;
    private TextView tv_overlay;

    private Button btn_add;
    private PinnedSectionListView lv_contacts;

    private ContactAdapter adapter;
    private List<ContactBean> contacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lb = (LetterBar) findViewById(R.id.lb);
        tv_overlay = (TextView) findViewById(R.id.tv_overlay);
        btn_add = (Button) findViewById(R.id.btn_add);
        lv_contacts = (PinnedSectionListView) findViewById(R.id.lv_contacts);
        lv_contacts.setShadowVisible(false);

        adapter = new ContactAdapter(this, contacts);
        lv_contacts.setAdapter(adapter);

        setContactsData();

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
                setContactsData();
            }
        });

        lv_contacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = adapter.getItem(position);
                if (item instanceof ContactBean){
                    ContactBean contact = (ContactBean) item;
                    showItemClickDialog(contact);
                }
            }
        });

        lv_contacts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = adapter.getItem(position);
                if(item instanceof ContactBean) {
                    ContactBean contact = (ContactBean) item;
                    ContactManager.deleteContact(MainActivity.this, contact);
                    setContactsData();
                }
                return true;
            }
        });

        lb.setOnLetterSelectedListener(new LetterBar.OnLetterSelectedListener() {
            @Override
            public void onLetterSelected(String letter) {
                if(TextUtils.isEmpty(letter)) {
                    tv_overlay.setVisibility(View.GONE);
                } else {
                    tv_overlay.setVisibility(View.VISIBLE);
                    tv_overlay.setText(letter);

                    int position = adapter.getLetterPosition(letter);
                    if(position != -1) {
                        lv_contacts.setSelection(position);
                    }
                }
            }
        });
    }

    private void setContactsData() {
        List<ContactBean> contactData = ContactManager.getContacts(this);
        contacts.clear();
        contacts.addAll(contactData);
        adapter.updateList();
    }

    private void showAddDialog() {
        View view = View.inflate(this, R.layout.dialog_contact, null);

        final EditText et_name = (EditText) view.findViewById(R.id.et_name);
        final EditText et_phone = (EditText) view.findViewById(R.id.et_phone);

        new AlertDialog.Builder(this).setTitle("添加联系人").setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!TextUtils.isEmpty(et_name.getText())) {
                            ContactBean contact = new ContactBean();
                            contact.setName(et_name.getText().toString());
                            contact.setPhone(et_phone.getText().toString());

                            ContactManager.addContact(MainActivity.this, contact);
                            setContactsData();
                        }
                    }
                }).setNegativeButton("取消",null).show();
    }

    private void showItemClickDialog(final ContactBean contact) {
        new AlertDialog.Builder(this)
                .setItems(new String[]{"拨打电话", "发送短信", "修改联系人"},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case 0:
                                        Intent intentCall = new Intent();
                                        intentCall.setAction(Intent.ACTION_CALL);
                                        intentCall.setData(Uri.parse("tel:" + contact.getPhone()));
                                        startActivity(intentCall);
                                        break;
                                    case 1:
                                        Intent intentSend = new Intent();
                                        intentSend.setAction(Intent.ACTION_SENDTO);
                                        intentSend.setData(Uri.parse("smsto://"+contact.getPhone()));
                                        startActivity(intentSend);
                                        break;
                                    case 2:
                                        showUpdateDialog(contact);
                                        setContactsData();
                                        break;
                                }
                            }
                        }).show();
    }

    private void showUpdateDialog(final ContactBean contact) {
        View view = View.inflate(this, R.layout.dialog_contact, null);

        final EditText et_name = (EditText) view.findViewById(R.id.et_name);
        final EditText et_phone = (EditText) view.findViewById(R.id.et_phone);

        et_name.setText(contact.getName());
        et_phone.setText(contact.getPhone());

        new AlertDialog.Builder(this).setTitle("修改联系人").setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!TextUtils.isEmpty(et_name.getText())) {
                            ContactBean updateContact = new ContactBean();
                            updateContact.setRawContactId(contact.getRawContactId());
                            updateContact.setName(et_name.getText().toString());
                            updateContact.setPhone(et_phone.getText().toString());

                            ContactManager.updateContact(MainActivity.this, updateContact);
                            setContactsData();
                        }
                    }
                }).setNegativeButton("取消",null).show();
    }
}
