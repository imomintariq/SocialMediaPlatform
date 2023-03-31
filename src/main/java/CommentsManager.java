import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.*;
import org.bson.conversions.*;
import org.bson.types.*;

import java.util.*;
import java.util.regex.*;

public class CommentsManager {
    private final MongoCollection<Document> commentsCollection;
    private final MongoDatabase database;

    public CommentsManager(MongoDatabase database) {
        this.database = database;
        this.commentsCollection = database.getCollection("comments");
        createIndexes();
    }

    private void createIndexes() {
        // Create index on the "postId" field, in ascending order
        commentsCollection.createIndex(Indexes.ascending("postId"));
        // Create index on the "creatorId" field, in ascending order
        commentsCollection.createIndex(Indexes.ascending("commenterId"));
    }

    public Document createComment(String commenterId, String postId, String comment) throws Exception {
        try {
            // Check if the post exists
            PostsManager postsManager = new PostsManager(database);
            Document post = postsManager.getPostById(postId);
            if (post == null) {
                throw new Exception("Error creating comment: Post with id " + postId + " doesn't exist...");
            } else {
                Document newComment = new Document("postId", new ObjectId(postId))
                        .append("commenterId", new ObjectId(commenterId))
                        .append("comment", comment);
                commentsCollection.insertOne(newComment);
                return newComment;
            }
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid creator ID or post ID format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error creating comment: " + e.getMessage());
        }
    }
    public void deleteComment(String commentId) throws Exception {
        try {
            // Check if the comment exists
            Document comment = commentsCollection.find(Filters.eq("_id", new ObjectId(commentId))).first();
            if (comment == null) {
                throw new Exception("Error deleting comment: Comment with id " + commentId + " doesn't exist...");
            } else {
                commentsCollection.deleteOne(Filters.eq("_id", new ObjectId(commentId)));
            }
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid comment ID format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error deleting comment: " + e.getMessage());
        }
    }

    public List<Document> getAllCommentsByPostId(String postId) {
        List<Document> comments = new ArrayList<>();
        try {
            //commentsCollection = database.getCollection("comments");
            ObjectId objectId = new ObjectId(postId);
            Document query = new Document("postId", objectId);
            comments = commentsCollection.find(query).into(new ArrayList<>());
        } catch (Exception e) {
            System.out.println("Error getting comments: " + e.getMessage());
        }
        return comments;
    }


    public List<Document> getAllCommentsForPost(String postTitle) throws Exception {
        List<Document> comments = new ArrayList<>();
        PostsManager postsManager = new PostsManager(database);
        Document post = postsManager.getPostByTitle(postTitle);
        if(post == null) {
            System.out.println("Post not found.");
            return comments;
        }

        String postId = post.getObjectId("_id").toString();
        CommentsManager commentsManager = new CommentsManager(database);
        comments = commentsManager.getAllCommentsByPostId(postId);

        return comments;
    }


}