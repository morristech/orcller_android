package pisces.instagram.sdk.model;

import java.util.Date;
import java.util.List;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/13/15.
 */
public class ApiInstagram {
    public class AccessTokenRes extends ApiInstagramResult {
        public String access_token;
        public String code;
        public User user;
    }

    public class UserListRes extends ApiInstagramResult {
        public List<User> data;
        public Pageination pageination;
    }

    public class MediaListRes extends ApiInstagramResult {
        public List<Media> data;
        public Pageination pageination;
    }

    public class UserRes extends ApiInstagramResult {
        public User data;
    }

    public class Media extends Model {
        public long created_time;
        public String type;
        public Images images;
    }

    public class Pageination extends Model {
        public String next_max_id;
        public String next_url;
    }

    public class Images extends Model {
        public Image low_resolution;
        public Image thumbnail;
        public Image standard_resolution;
    }

    public class Image extends Model {
        public int width;
        public int height;
        public String url;
    }

    public class User extends Model {
        public long id;
        public String bio;
        public String full_name;
        public String profile_picture;
        public String username;
        public String website;
    }
}