package com.zhang.contactdemo;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mr.Z on 2016/11/19 0019.
 */

public class ContactAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter {

    private static final int VIEW_TYPE_CONTACT = 0;
    private static final int VIEW_TYPE_LETTER = 1;

    private Context context;
    private List<ContactBean> contacts;

    private List<Object> datas;
    private Map<String, Integer> letterPosition;

    public ContactAdapter(Context context, List<ContactBean> contacts) {
        this.context = context;
        this.contacts = contacts;
        initList();
    }

    private void initList() {
        datas = new ArrayList<>();
        letterPosition = new HashMap<>();
        Collections.sort(contacts, new Comparator<ContactBean>() {
            @Override
            public int compare(ContactBean contactBean, ContactBean t1) {
                String lhsName = PinYinUtils.trans2PinYin(contactBean.getName()).toUpperCase();
                String rhsName = PinYinUtils.trans2PinYin(t1.getName()).toUpperCase();
                return lhsName.compareTo(rhsName);
            }
        });

        for (int i = 0; i < contacts.size(); i++) {
            ContactBean contact = contacts.get(i);

            String firstLetter = getFirstLetter(contact.getName());
            if (!letterPosition.containsKey(firstLetter)) {
                letterPosition.put(firstLetter, datas.size());
                datas.add(firstLetter);
            }

            datas.add(contact);
        }

    }

    private String getFirstLetter(String name) {
        String firstLetter = "";
        char c = PinYinUtils.trans2PinYin(name).toUpperCase().charAt(0);
        if (c >= 'A' && c <= 'Z') {
            firstLetter = String.valueOf(c);
        }
        return firstLetter;
    }

    public int getLetterPosition(String letter) {
        Integer positoin = letterPosition.get(letter);
        return positoin == null ? -1 : positoin;
    }

    public void updateList() {
        initList();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int i) {
        return datas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        int itemViewType = getItemViewType(i);
        return itemViewType == VIEW_TYPE_CONTACT ?
                getContactView(i, view) : getLetterView(i, view);
    }

    private View getContactView(int i, View view) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = View.inflate(context, R.layout.item_contact, null);
            holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
            holder.tv_phone = (TextView) view.findViewById(R.id.tv_phone);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        ContactBean contact = (ContactBean) getItem(i);

        holder.tv_name.setText(contact.getName());
        holder.tv_phone.setText(contact.getPhone());
        return view;
    }

    private View getLetterView(int i, View view) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = View.inflate(context, R.layout.item_letter, null);
            holder.tv_letter = (TextView) view.findViewById(R.id.tv_letter);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        String letter = (String) getItem(i);

        holder.tv_letter.setText(letter);

        return view;
    }

    private static class ViewHolder {
        TextView tv_letter;
        TextView tv_name;
        TextView tv_phone;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return datas.get(position) instanceof ContactBean ?
                VIEW_TYPE_CONTACT : VIEW_TYPE_LETTER;
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == VIEW_TYPE_LETTER;
    }
}
