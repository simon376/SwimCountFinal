package de.oth_regensburg.mueller.simon.swimcount.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(tableName = "users")
public class User {

    @Expose
    @PrimaryKey(autoGenerate = true)
    private  int startnummer;

    @Expose
    @ColumnInfo(name = "first_name")
    private  String vorname;

    @Expose
    @ColumnInfo(name = "last_name")
    private  String nachname;

    @Expose
    @ColumnInfo(name = "distance")
    private int strecke;


    @Expose
    @ColumnInfo(name = "imagePath")
    private String fotoPfad;


    private Date lastRefresh;     //Not included in JSON


    public User() {
        this.vorname = "Hans";
        this.nachname = "Dampf";
        this.strecke = -1;
        this.fotoPfad = "";
    }

    @Ignore
    public User(int startnummer, String vorname, String nachname, int strecke, String fotoPfad, Date lastRefresh) {
        this.startnummer = startnummer;
        this.vorname = vorname;
        this.nachname = nachname;
        this.strecke = strecke;
        this.fotoPfad = fotoPfad;
        this.lastRefresh = lastRefresh;
    }

    @Ignore
    public User(int startnummer, String vorname, String nachname, int strecke) {
        this.startnummer = startnummer;
        this.vorname = vorname;
        this.nachname = nachname;
        this.strecke = strecke;
    }



    // --- GETTER ---

    public int getStartnummer() {
        return startnummer;
    }
    public String getVorname() {
        return vorname;
    }
    public String getNachname() {
        return nachname;
    }
    public String getName() {
        return (this.vorname + " " + this.nachname);
    }
    public String getFotoPfad() {
        return fotoPfad;
    }
    public int getStrecke() {
        return strecke;
    }
    public Date getLastRefresh() {
        return lastRefresh;
    }


    // --- SETTER ---

    public void setStrecke(int strecke) {
        this.strecke = strecke;
    }
    public void setStartnummer(int startnummer) {
        this.startnummer = startnummer;
    }
    public void setVorname(String vorname) {
        this.vorname = vorname;
    }
    public void setNachname(String nachname) {
        this.nachname = nachname;
    }
    public void setFotoPfad(String pfad) {
        this.fotoPfad = pfad;
    }
    public void setLastRefresh(Date lastRefresh) {
        this.lastRefresh = lastRefresh;
    }


    public void addStrecke(int add) { this.strecke += add;}


    //nur für Testzwecke
    @Deprecated
    public static ArrayList<User> createUserList(int numUsers){
        ArrayList<User> users = new ArrayList<>();


        for(int i = 0; i < numUsers; i++){
            users.add(new User(i+100, "Hans", "Dampf", (i*100) ));
        }

        return  users;
    }

    //nur für Testzwecke
    public static List<User> createUserListGSON(String response)  {

        Gson gson = new GsonBuilder().create();

        Type collectionType = new TypeToken<ArrayList<User>>() {}.getType();

        return gson.fromJson(response, collectionType);
    }


    //nicht verwendet
    @Deprecated
    public static User fromJSON(JSONObject jsonObject){

        int nummer, strecke;
        String vorname, nachname;
        try {
            nummer = jsonObject.getInt("nummer");
            vorname = jsonObject.getString("vorname");
            nachname = jsonObject.getString("nachname");
            strecke = jsonObject.getInt("strecke");

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return new User(nummer, vorname, nachname, strecke);

    }


}
