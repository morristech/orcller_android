package pisces.instagram.sdk.model;

import org.json.JSONObject;

import java.util.List;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/13/15.
 */
public class ApiInstagram {

    // ================================================================================================
    //  Response
    // ================================================================================================

    public class AccessTokenRes extends ApiInstagramResult {
        public String access_token;
        public String code;
        public User user;
    }

    public class ListRes extends ApiInstagramResult {
        public Pagination pagination;

        public Pagination getPagination() {
            if (pagination == null) {
                pagination = new Pagination();
            }
            return pagination;
        }
    }

    public class MediaListRes extends ListRes {
        public List<Media> data;
    }

    public class UserListRes extends ListRes {
        public List<User> data;
    }

    public class UserRes extends ApiInstagramResult {
        public User data;
    }

    // ================================================================================================
    //  Model
    // ================================================================================================

    public static class Media extends Model {
        public enum Type {
            Image("image"),
            Video("video");

            private String type;

            private Type(String type) {
                this.type = type;
            }

            public String getValue() {
                return type;
            }
        }

        public boolean user_has_liked;
        public long created_time;
        public String attribution;
        public String filter;
        public String id;
        public String link;
        public String type;
        public String[] tags;
        public JSONObject comments;
        public Caption caption;
        public Images images;
        public Likes likes;
        public Location location;
        public Videos videos;

        public boolean isVideo() {
            return Type.Video.getValue().equals(type);
        }
    }

    public static class Caption extends Model {
        public long id;
        public long created_time;
        public String text;
        public User from;
    }

    public static class Images extends Model {
        public Image low_resolution;
        public Image thumbnail;
        public Image standard_resolution;
    }

    public static class Image extends Model {
        public int width;
        public int height;
        public String url;
    }

    public static class Likes extends Model {
        public int count;
        public List<User> data;
    }

    public static class Location extends Model {
        public long id;
        public float latitude;
        public float longitude;
        public String name;
    }

    public static class Pagination extends Model {
        public String next_max_id;
        public String next_url;

        public boolean hasNext() {
            return next_max_id != null;
        }
    }

    public static class Videos extends Model {
        public Video low_resolution;
        public Video standard_resolution;
    }

    public static class Video extends Model {
        public int width;
        public int height;
        public String url;
    }

    public static class User extends Model {
        public long id;
        public String bio;
        public String full_name;
        public String profile_picture;
        public String username;
        public String website;
    }
}
