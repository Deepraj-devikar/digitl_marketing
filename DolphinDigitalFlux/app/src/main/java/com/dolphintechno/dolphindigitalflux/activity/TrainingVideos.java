package com.dolphintechno.dolphindigitalflux.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.dolphintechno.dolphindigitalflux.R;
import com.dolphintechno.dolphindigitalflux.adapter.AdapterTrainingVideos;
import com.dolphintechno.dolphindigitalflux.adapter.AdapterVideosCategory;
import com.dolphintechno.dolphindigitalflux.data.MySharedPreferences;
import com.dolphintechno.dolphindigitalflux.helper.SharedPrefKeys;
import com.dolphintechno.dolphindigitalflux.helper.URLs;
import com.dolphintechno.dolphindigitalflux.model.VideoInfo;
import com.dolphintechno.dolphindigitalflux.model.VideosCategory;
import com.dolphintechno.dolphindigitalflux.singleton.MyVolleySingleton;
import com.dolphintechno.dolphindigitalflux.utils.Tools;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainingVideos extends AppCompatActivity implements View.OnClickListener {

    MySharedPreferences dataProccessor;

    private ActionBar actionBar;
    private Toolbar toolbar;
    private Menu menu_navigation;
    private DrawerLayout drawer;
    private View navigation_header;
    private boolean is_account_mode = false;
    private boolean isSearchCategoryBtnClicked = false;

    TextView tv_drawer_header_name, tv_drawer_header_email;

    String strFirstName;
    String strLastName;
    String strEmail;
    String strName;
    String str_unique_id;

    String profile_pic_name;
    CircularImageView avatar;

    Button btn_search_vdos_by_by_cat;

    RecyclerView recy_view_training_videos;
    LinearLayoutManager linearLayoutManager;
    AdapterTrainingVideos adapterTrainingVideos;

    List<VideoInfo> videoInfoList;
    VideoInfo videoInfo;

    RecyclerView rec_v_tra_vdo_srch_cat;
    LinearLayoutManager llmCategory;
    AdapterVideosCategory adapterVideosCategory;

    List<VideosCategory> videosCategoryList;
    VideosCategory videosCategory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_videos);

        initNavigationMenu();
        avatar = (CircularImageView) navigation_header.findViewById(R.id.avatar);

        btn_search_vdos_by_by_cat = (Button) findViewById(R.id.btn_search_vdos_by_by_cat);

        dataProccessor = new MySharedPreferences(this);

        /*
         * Fetching User Id For Recognizing User
         * So We Can Get Users Info.
         */
        str_unique_id = dataProccessor.getStr(SharedPrefKeys.user_id);

        recy_view_training_videos = (RecyclerView) findViewById(R.id.recy_view_training_videos);
        recy_view_training_videos.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        recy_view_training_videos.setLayoutManager(linearLayoutManager);



        rec_v_tra_vdo_srch_cat = (RecyclerView) findViewById(R.id.rec_v_tra_vdo_srch_cat);
        rec_v_tra_vdo_srch_cat.setHasFixedSize(true);
        llmCategory = new LinearLayoutManager(this);
        rec_v_tra_vdo_srch_cat.setLayoutManager(llmCategory);



        fetchVideoInfo();

        setInfo();
        initToolbar();

        btn_search_vdos_by_by_cat.setOnClickListener(this);

    }

    private void fetchVideoCategory() {

        rec_v_tra_vdo_srch_cat.setVisibility(View.VISIBLE);

        StringRequest stringRequestCategory = new StringRequest(Request.Method.POST, URLs.url_fetch_video_category,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean(URLs.keyVideosCategoryIsFetched)){
                                videosCategoryList = new ArrayList<>();
                                JSONArray jsonCategoryList = jsonObject.getJSONArray(URLs.keyVideosCategoryList);
                                for (int i = 0; i < jsonCategoryList.length(); i++ ){
                                    videosCategory = new VideosCategory();
                                    videosCategory.setVidedoCategoryName(jsonCategoryList.getString(i));
                                    videosCategoryList.add(videosCategory);
                                }
                                adapterVideosCategory = new AdapterVideosCategory(videosCategoryList, getApplicationContext());
                                rec_v_tra_vdo_srch_cat.setAdapter(adapterVideosCategory);
                            }

                            adapterVideosCategory.setOnItemClickListener(new AdapterVideosCategory.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterVideosCategory.VideoCategoryViewHolder view, VideosCategory obj, int pos) {
                                    fetchVideosByCategories(obj.getVidedoCategoryName());
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(stringRequestCategory);
    }

    void fetchVideosByCategories(final String searchCategory){

        rec_v_tra_vdo_srch_cat.setVisibility(View.GONE);

        StringRequest stringRequestVdoByCat = new StringRequest(Request.Method.POST, URLs.url_fetch_video_by_categories,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean(URLs.keyTrainingVideoIsFetch)){
                                videoInfoList = new ArrayList<>();
                                JSONArray jsonVideoList = jsonObject.getJSONArray(URLs.keyTrainingVideoList);
                                for (int i = 0; i < jsonVideoList.length(); i++){
                                    JSONObject jsonVideo = jsonVideoList.getJSONObject(i);
                                    videoInfo = new VideoInfo();
                                    videoInfo.setvId(jsonVideo.getString(URLs.keyTrainingVideoID));
                                    videoInfo.setvCtg(jsonVideo.getString(URLs.keyTrainingVideoCategory));
                                    videoInfo.setvLink(jsonVideo.getString(URLs.keyTrainingVideoLink));
                                    videoInfo.setvDesc(jsonVideo.getString(URLs.keyTrainingVideoDescription));
                                    videoInfo.setAuthName(jsonVideo.getString(URLs.keyTrainingVideoName));
                                    videoInfo.setTime(jsonVideo.getString(URLs.keyTrainingVideoTime));
                                    videoInfo.setYouTubeVideoId(jsonVideo.getString(URLs.keyTrainingVideoYouTubeVideoId));
                                    videoInfoList.add(videoInfo);
                                }
                                adapterTrainingVideos = new AdapterTrainingVideos(videoInfoList, getApplicationContext());
                                recy_view_training_videos.setAdapter(adapterTrainingVideos);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("scat", searchCategory);
                return params;
            }
        };
        MyVolleySingleton.getInstance(this).addToRequestQueue(stringRequestVdoByCat);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Training Videos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this);
    }

    private void setInfo() {
        strFirstName = dataProccessor.getStr(SharedPrefKeys.first_name);
        strLastName = dataProccessor.getStr(SharedPrefKeys.last_name);
        strEmail = dataProccessor.getStr(SharedPrefKeys.email_id);
        strName = strFirstName+" "+strLastName;

        tv_drawer_header_name = (TextView) navigation_header.findViewById(R.id.tv_drawer_header_name);
        tv_drawer_header_email = (TextView) navigation_header.findViewById(R.id.tv_drawer_header_email);
        tv_drawer_header_name.setText(strName);
        tv_drawer_header_email.setText(strEmail);


        profile_pic_name = dataProccessor.getStr(SharedPrefKeys.profile_pic);

        String strUrlAvtarImg = URLs.url_profile_pic+profile_pic_name;
//                            Toast.makeText(getApplicationContext(), "--"+strUrlAvtarImg, Toast.LENGTH_LONG).show();
        ImageRequest imageRequest = new ImageRequest(strUrlAvtarImg, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                avatar.setImageBitmap(response);
            }
        }, 0, 0, ImageView.ScaleType.CENTER, Bitmap.Config.RGB_565,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(imageRequest);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getTitle().equals("Search")) {
//            Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_LONG).show();
            fetchVideoCategory();
        }else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


    private void initNavigationMenu() {
        final NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view_training_videos);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout_training_videos);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                updateCounter(nav_view);
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem item) {
                onItemNavigationClicked(item);
                return true;
            }
        });

        updateCounter(nav_view);
        menu_navigation = nav_view.getMenu();

        // navigation header
        navigation_header = nav_view.getHeaderView(0);
    }

    private void onItemNavigationClicked(MenuItem item) {



        if (!is_account_mode) {
            drawer.closeDrawers();
            navigate(item.getTitle());

        } else {
            CircularImageView avatar = (CircularImageView) navigation_header.findViewById(R.id.avatar);
            switch (item.getItemId()) {
                case 1000:
                    avatar.setImageResource(R.drawable.photo_male_5);
                    break;
                case 2000:
                    avatar.setImageResource(R.drawable.photo_male_2);
                    break;
                default:
                    Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private void navigate(CharSequence title) {
        if (title.equals("Dashboard")){
            Intent intent = new Intent(this, HomeScreen.class);
            startActivity(intent);
        }else if (title.equals("Wallet")){
            Intent intent = new Intent(this, WalletHistory.class);
            startActivity(intent);
        }else if (title.equals("Team")){
            Intent intent = new Intent(this, NetworkList.class);
            startActivity(intent);
        }else if (title.equals("Add User")){
            Intent intent = new Intent(this, AddNew.class);
            startActivity(intent);
        }else if (title.equals("Withdraw")){
            Intent intent = new Intent(this, Withdraw.class);
            startActivity(intent);
        }else if (title.equals("Shop")){
            Intent intent = new Intent(this, Shop.class);
            startActivity(intent);
        }else if (title.equals("Cart")){
            Intent intent = new Intent(this, Cart.class);
            startActivity(intent);
        }else if (title.equals("Add Product")){
            Intent intent = new Intent(this, AddProduct.class);
            startActivity(intent);
        }else if (title.equals("Attendance")){
            Intent intent = new Intent(this, Attendance.class);
            startActivity(intent);
        }else if (title.equals("Taks")){
            Intent intent = new Intent(this, Task.class);
            startActivity(intent);
        }else if (title.equals("FAQ")){
            Intent intent = new Intent(this, HomeScreen.class);
            startActivity(intent);
//            sendWhatsappContacts();
        }else if (title.equals("Company")){
            Intent intent = new Intent(this, Company.class);
            startActivity(intent);
        }else if (title.equals("Share")){
            Intent intent = new Intent(this, ShareOnWhatsApp.class);
            startActivity(intent);
        }else if (title.equals("Contacts")){
            Intent intent = new Intent(this, WhatsappNos.class);
            startActivity(intent);
        }else if (title.equals("Add Contact")){
            Intent intent = new Intent(this, AddWhatsappNumber.class);
            startActivity(intent);
        }else if (title.equals("Rate Us")){
            Intent intent = new Intent(this, RateUs.class);
            startActivity(intent);
        }else if (title.equals("Help Line")){
            Intent intent = new Intent(this, HomeScreen.class);
            startActivity(intent);
        }else if (title.equals("Logout")){
            Intent intent = new Intent(this, Logout.class);
            startActivity(intent);
        }else if (title.equals("Settings")){
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        }else if (title.equals("Help &amp; Feedback")){
            Intent intent = new Intent(this, HomeScreen.class);
            startActivity(intent);
        }
    }

    private void updateCounter(NavigationView nav) {
        if (is_account_mode) return;
        Menu m = nav.getMenu();
    }

    private void fetchVideoInfo(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.url_fetch_video,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean(URLs.keyTrainingVideoIsFetch)){
                                videoInfoList = new ArrayList<>();
                                JSONArray jsonVideoList = jsonObject.getJSONArray(URLs.keyTrainingVideoList);
                                for (int i = 0; i < jsonVideoList.length(); i++){
                                    JSONObject jsonVideo = jsonVideoList.getJSONObject(i);
                                    videoInfo = new VideoInfo();
                                    videoInfo.setvId(jsonVideo.getString(URLs.keyTrainingVideoID));
                                    videoInfo.setvCtg(jsonVideo.getString(URLs.keyTrainingVideoCategory));
                                    videoInfo.setvLink(jsonVideo.getString(URLs.keyTrainingVideoLink));
                                    videoInfo.setvDesc(jsonVideo.getString(URLs.keyTrainingVideoDescription));
                                    videoInfo.setAuthName(jsonVideo.getString(URLs.keyTrainingVideoName));
                                    videoInfo.setTime(jsonVideo.getString(URLs.keyTrainingVideoTime));
                                    videoInfo.setYouTubeVideoId(jsonVideo.getString(URLs.keyTrainingVideoYouTubeVideoId));
                                    videoInfoList.add(videoInfo);
                                }
                                adapterTrainingVideos = new AdapterTrainingVideos(videoInfoList, getApplicationContext());
                                recy_view_training_videos.setAdapter(adapterTrainingVideos);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

    }


    @Override
    public void onClick(View v) {
        if (v == btn_search_vdos_by_by_cat){
            if (isSearchCategoryBtnClicked){
                rec_v_tra_vdo_srch_cat.setVisibility(View.GONE);
                isSearchCategoryBtnClicked = false;
            }else {
                isSearchCategoryBtnClicked = true;
                fetchVideoCategory();
            }

        }
    }
}
