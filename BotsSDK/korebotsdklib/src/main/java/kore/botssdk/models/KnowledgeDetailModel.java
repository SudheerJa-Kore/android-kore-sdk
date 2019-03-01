package kore.botssdk.models;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;

import androidx.databinding.BindingAdapter;
import kore.botssdk.utils.DateUtils;
import kore.botssdk.utils.StringUtils;


/**
 * Created by Shiva Krishna on 1/30/2018.
 */

public class KnowledgeDetailModel  {
    private String streamId;
    private String creator;
    private String lMod;
    private String imageUrl;
    private String title;
    private ContactInfo owner;
    private String url;
    private int nLikes;

    public long getLastMod() {
        return lastMod;
    }

    public void setLastMod(long lastMod) {
        this.lastMod = lastMod;
    }

    private long lastMod;
    public String getSharedBy() {
        return sharedBy;
    }
    public String getFormattedModifiedDate() {
        if(formattedModifiedDate == null){
            formattedModifiedDate =   DateUtils.formattedSentDateV6((Long) lastMod);
        }
        return formattedModifiedDate;
    }

    public String getFormattedHeaderDate() {
        if(formattedHeaderDate == null){
            formattedHeaderDate = DateUtils.formattedSentDateV8((Long) lastMod,false);
        }
        return formattedHeaderDate;
    }

    private String formattedHeaderDate;

    public void setFormattedModifiedDate(String formattedModifiedDate) {
        this.formattedModifiedDate = formattedModifiedDate;
    }
    public String getLastModifiedDate(){
        return "Modified "+ DateUtils.formattedSentDateV8(lastMod,true);
    }

    private String formattedModifiedDate;


    public void setSharedBy(String sharedBy) {
        this.sharedBy = sharedBy;
    }

    private String sharedBy;
    public long getNViews() {
        return nViews;
    }

    public void setNViews(long nViews) {
        this.nViews = nViews;
    }

    private long nViews;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    private int count;

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getlMod() {
        return lMod;
    }

    public void setlMod(String lMod) {
        this.lMod = lMod;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }

    public ArrayList<KoreComponentModel> getComponents() {
        return components;
    }

    public void setComponents(ArrayList<KoreComponentModel> components) {
        this.components = components;
    }

    public ArrayList<LinkPreviewModel> getLinkPreviews() {
        return linkPreviews;
    }

    public void setLinkPreviews(ArrayList<LinkPreviewModel> linkPreviews) {
        this.linkPreviews = linkPreviews;
    }

    private String pId;
    String id;

    String type;
    private String orgId;
    private String desc;
    private long createdOn;
    private String description;

    public ArrayList<String> getHashTag() {
        return hashTag;
    }

    public void setHashTag(ArrayList<String> hashTag) {
        this.hashTag = hashTag;
    }

    private ArrayList<String> hashTag;

    private ArrayList<KoreComponentModel> components;
    private ArrayList<LinkPreviewModel> linkPreviews;
    private ArrayList<String> likes;

    public ArrayList<String> getFollowers() {
        return followers;
    }

    public void setFollowers(ArrayList<String> followers) {
        this.followers = followers;
    }

    private ArrayList<String> followers;
    public int getnShares() {
        return nShares;
    }

    public void setnShares(int nShares) {
        this.nShares = nShares;
    }

    private int nShares;

    public int getNComments() {
        return nComments;
    }

    public void setNComments(int nComments) {
        this.nComments = nComments;
    }

    private int nComments;

    public ArrayList<CommentModel> getComments() {
        return comments;
    }

    public void setComments(ArrayList<CommentModel> comments) {
        this.comments = comments;
    }

    private ArrayList<CommentModel> comments;

    public int getnFollows() {
        return nFollows;
    }

    public void setnFollows(int nFollows) {
        this.nFollows = nFollows;
    }

    private int nFollows;

    public MyActions getMyActions() {
        return myActions;
    }

    public void setMyActions(MyActions myActions) {
        this.myActions = myActions;
    }

    private MyActions myActions;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ContactInfo getOwner() {
        return owner;
    }

    public void setOwner(ContactInfo owner) {
        this.owner = owner;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNLikes() {
        return nLikes;
    }

    public void setNLikes(int nLikes) {
        this.nLikes = nLikes;
    }

    public ArrayList<String> getLikes() {
        return likes;
    }

    public void setLikes(ArrayList<String> likes) {
        this.likes = likes;
    }


    public class VoteModel {
         int vote;

       public int getVote() {
           return vote;
       }

       public void setVote(int vote) {
           this.vote = vote;
       }

       public String getBy() {
           return by;
       }

       public void setBy(String by) {
           this.by = by;
       }

       String by;
   }

   public class MyActions{
       boolean like;
       boolean follow;

       public int getPrivilege() {
           return privilege;
       }

       public void setPrivilege(int privilege) {
           this.privilege = privilege;
       }

       int privilege;

       public boolean isLike() {
           return like;
       }

       public void setLike(boolean like) {
           this.like = like;
       }

       public boolean isFollow() {
           return follow;
       }

       public void setFollow(boolean follow) {
           this.follow = follow;
       }
   }

    public class CommentModel {
        private long cOn;
        private String id;
        private long lMod;
        private String by;

        public long getcOn() {
            return cOn;
        }

        public void setcOn(long cOn) {
            this.cOn = cOn;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public long getlMod() {
            return lMod;
        }

        public void setlMod(long lMod) {
            this.lMod = lMod;
        }

        public String getBy() {
            return by;
        }

        public void setBy(String by) {
            this.by = by;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        private String comment;
        private String label;

    }

    public class ContactInfo{
        public String getlN() {
            return lN;
        }

        public void setlN(String lN) {
            this.lN = lN;
        }

        private String lN;
        private String fN;
        private String id;



        public String getfN() {
            return fN;
        }

        public void setfN(String fN) {
            this.fN = fN;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getEmailId() {
            return emailId;
        }

        public void setEmailId(String emailId) {
            this.emailId = emailId;
        }

        private String color;
        private String emailId;

        public String getInitials(){
           return StringUtils.getInitials(fN,lN);
        }
        public String getName(){
            return fN+" "+lN;
        }
    }
    @Override
    public boolean equals(Object obj) {
        if(obj ==null) return false;
        if (obj == this) return true;
        if(obj instanceof KnowledgeDetailModel){
            if(this.getId() != null && ((KnowledgeDetailModel) obj).getId() != null) {
                return this.getId().equals(((KnowledgeDetailModel) obj).getId());
            }
        }
        return false;
    }

    /**
     * to avoid same hash code generation if any of fields are equal like in one object email and other object kore id are same and remaining or null
     * for that we are multiplying with different numbers
     * @return
     */
    @Override
    public int hashCode() {
        int result = id == null ? 0 : 31 * id.hashCode();
        result = 32 * result + (id == null ? 0 : id.hashCode());
        result = 33 * result + (creator == null ? 0 : creator.hashCode());
        return result;
    }
    public Spanned getSpannedString() {

        return StringUtils.isNullOrEmpty(desc) ? null : Html.fromHtml(StringEscapeUtils.unescapeHtml4(desc.replaceAll("<br>", " ")));
    }

}
