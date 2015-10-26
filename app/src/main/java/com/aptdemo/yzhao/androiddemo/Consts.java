package com.aptdemo.yzhao.androiddemo;

/**
 * Created by Yicong on 10/23/15.
 */
public final class Consts {
    public static final String API_STREAM_NEARBY_URL = "http://just-plate-107116.appspot.com/mobile/stream_nearby";
    public static final String API_AUTOCOMPLETE_URL = "http://just-plate-107116.appspot.com/api/autocomplete";
    public static final String API_STREAM_AUTOCOMPLETE_URL = "http://just-plate-107116.appspot.com/api/stream_autocomplete";
    public static final String API_SEARCH_STREAM_URL = "http://just-plate-107116.appspot.com/api/mobile_search";
    public static final String API_STREAM_LIST_URL = "http://just-plate-107116.appspot.com/mobile/stream_list";
    public static final String API_STREAM_VIEW_URL = "http://just-plate-107116.appspot.com/mobile/stream_view";
    public static final String API_STREAM_SUBSCRIBED_URL = "http://just-plate-107116.appspot.com/mobile/stream_subscribed";

    public static final String VIEW_ALL_PHOTOS_URL="http://aptandroiddemo.appspot.com/viewAllPhotos"; // url used in demo
    public static final String DEFAULT_COVER_URL = "http://www.clipartbest.com/cliparts/LiK/rb7/LiKrb7gnT.png";
    public static final int PHOTO_QUALITY = 25; //quality for the photo taken

    public static final int PICTURE_SIZE_MAX_WIDTH = 1280;
    public static final int PREVIEW_SIZE_MAX_WIDTH = 640;

    // Num of streams shown on one search page
    public static final int SEARCH_STREAM_PER_PAGE = 6;
    public static final int VIEW_A_STREAM_PER_PAGE = 6;
    public static final int VIEW_ALL_STREAM_PER_PAGE = 6;
    public static final int NEARBY_PER_PAGE = 6;

    public static final String KEYWORD_NAME = "keywords"; // name of keywords for passing parameters between activities
    public static final String USER_EMAIL_NAME = "user_email";
    // warnings
    public static final String SEARCH_KEYWORD_EMPTY_WARNING = "No keywords!";

    // String for button
    public static final String SUBSCRIBE_BUTTON_SHOW_ALL = "Show All Streams";
    public static final String SUBSCRIBE_BUTTON_SHOW_SUBSCRIBE = "My Subscribed Streams";

    public enum AutocompleteType{
        ALL,
        STREAM_NAME
    }

    public static final int RESULT_CODE_TAKE_PHOTO = 1;
}
