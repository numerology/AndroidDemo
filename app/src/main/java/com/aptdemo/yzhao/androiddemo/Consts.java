package com.aptdemo.yzhao.androiddemo;

/**
 * Created by Yicong on 10/23/15.
 */
public final class Consts {
    public static final String API_AUTOCOMPLETE_URL = "http://just-plate-107116.appspot.com/api/autocomplete";
    public static final String API_STREAM_AUTOCOMPLETE_URL = "http://just-plate-107116.appspot.com/api/stream_autocomplete";
    public static final String API_SEARCH_STREAM_URL = "http://connexus-yw.appspot.com/api/mobile_search"; //TODO: Change this address to just-plate address after updating just-plate
    public enum AutocompleteType{
        ALL,
        STREAM_NAME
    }
}
