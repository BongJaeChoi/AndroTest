package org.todaysays;

public class ListItem {
    private String[] mData;


    public ListItem(String imgurl, String txt1, String txt2){
        mData = new String[3];
        mData[0] = imgurl;
        mData[1] = txt1;
        mData[2] = txt2;
    }

    public String[] getData(){
        return mData;
    }

    public String getData(int index){
        return mData[index];
    }

    public void setData(String[] data){
        mData = data;
    }
}
