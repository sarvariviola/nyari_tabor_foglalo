package com.example.nyaritabor;

public class BookingCamp {
    private String id;
    private String name;
    private String info;
    private int price;
    private float ratedInfo;
    private int imageResource;
    private String ageGroup;
    private String date;
    private int freePlace;
    private String place;


    public BookingCamp(){}

    public BookingCamp(int imageResource, String info, String name, int price, float ratedInfo, String ageGroup, String date, int freePlace, String place) {
        this.imageResource = imageResource;
        this.info = info;
        this.name = name;
        this.price = price;
        this.ratedInfo = ratedInfo;
        this.ageGroup = ageGroup;
        this.date = date;
        this.freePlace = freePlace;
        this.place = place;
    }
    public String getInfo() {
        return info;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public float getRatedInfo() {
        return ratedInfo;
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getAgeGroup() {
        return ageGroup;
    }

    public String getDate() {
        return date;
    }
    public String _getId(){
        return id;
    }

    public void setId(String id){
        this.id=id;
    }
    public int getFreePlace(){
        return freePlace;
    }
    public void setFreePlace(int freePlace) {
        this.freePlace = freePlace;
    }
    public String getPlace() {
        return place;
    }
}
