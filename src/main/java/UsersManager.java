import com.mongodb.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

public class UsersManager {

    private final MongoCollection<Document> usersCollection;
    private final MongoDatabase database;

    public UsersManager(MongoDatabase database) throws MongoException {
        this.database = database;
        this.usersCollection = database.getCollection("users");
        createIndexes();
    }

    public boolean userExists(String username) throws Exception {
        try {
            System.out.println("User: "+getUserByUsername(username));
            return getUserByUsername(username) != null;
        } catch (Exception e) {
            throw new Exception("Error checking if user exists: " + e.getMessage());
        }
    }

    public String getUserId(String username) throws MongoException {
        try {
            Document query = new Document("username", username);
            Document user = usersCollection.find(query).first();
            if (user != null) {
                ObjectId userId = user.getObjectId("_id");
                return userId.toHexString();
            } else {
                // Handle the case where no user was found...
                System.out.println("User Does Not Exist");
                return null;
            }
        } catch (MongoException e) {
            throw new MongoException("Error getting user ID: " + e.getMessage());
        }
    }

    public void createUser(String username, String email, String password){
        if(getUserByUsername(username) != null){
            System.out.println("Cant create this user because User With the ID:\"+ username +\" already exists");
            //throw new MongoException("Error: Cant create this user because User With the ID:"+ username +" already exists");
        }
        else{
            Document newUser = new Document("username", username)
                    .append("email", email)
                    .append("password", password);
            try {
                usersCollection.insertOne(newUser);
            } catch (MongoException e) {
                throw new MongoException("Error creating user: " + e.getMessage());
            }
        }
    }

    public Document getUserById(String id) throws MongoException {
        ObjectId objectId = new ObjectId(id);
        Document query = new Document("_id", objectId);
        try {
            Document userById = usersCollection.find(query).first();
            if (userById != null) {
                return userById;
            } else {
                // Handle the case where no user was found...
                System.out.println("User Doest Exist");
                return null;
                //throw new MongoException("Error: User with id" + id + "doesnt exist ...");
            }

        } catch (MongoException e) {
            throw new MongoException("Error getting user by ID: " + e.getMessage());
        }
    }

    public Document getUserByUsername(String username) throws MongoException {
        Document query = new Document("username", username);
        try {
            Document userByUsername = usersCollection.find(query).first();
            if (userByUsername != null) {
                return userByUsername;
            } else {
                // Handle the case where no user was found...
                System.out.println("User Doest Exist");
                return null;
                //throw new MongoException("Error: User with username" + username + "doesnt exist ...");
            }
        } catch (MongoException e) {
            throw new MongoException("Error getting user by username: " + e.getMessage());
        }
    }

    public void updateUserById(String id, String username, String email, String password) throws MongoException {
        try {
            ObjectId objectId = new ObjectId(id);
            Document filter = new Document("_id", objectId);
            Document update = new Document("$set", new Document("username", username)
                    .append("email", email)
                    .append("password", password));
            usersCollection.updateOne(filter, update);
        } catch (IllegalArgumentException e) {
            throw new MongoException("Invalid Object ID");
        } catch (MongoException e) {
            throw new MongoException("Error updating user: " + e.getMessage());
        }
    }


    public void deleteUser(String id) throws MongoException {
        ObjectId objectId = new ObjectId(id);
        Document query = new Document("_id", objectId);
        try {
            usersCollection.deleteOne(query);
        } catch (MongoException e) {
            throw new MongoException("Error deleting user: " + e.getMessage());
        }
    }

    private void createIndexes() throws MongoException {
        try {
            usersCollection.createIndex(Indexes.ascending("username"), new IndexOptions().unique(true));
        } catch (MongoCommandException e) {
            throw new MongoException("Error creating index: " + e.getErrorMessage());
        }
    }

    public void shardCollection() throws MongoException {
        try {
            Document shardKey = new Document("_id", "hashed");
            usersCollection.createIndex(shardKey);
            Document command = new Document("shardCollection", usersCollection.getNamespace().toString())
                    .append("key", shardKey);
            MongoClient mongoClient = new MongoClient("localhost", 27017);
            mongoClient.getDatabase("admin").runCommand(command);
            System.out.println("Collection sharded.");
        } catch (MongoException e) {
            throw new MongoException("Error sharding collection: " + e.getMessage());
        }
    }

    public boolean userIdExists(String id) throws Exception {
        try {
            ObjectId userId = new ObjectId(id);
            Document query = new Document("_id", userId);
            System.out.println("User: " + usersCollection.countDocuments(query));
            return usersCollection.countDocuments(query) > 0;
        } catch (IllegalArgumentException e) {
            // Handle the case where the given id is not a valid ObjectId
            System.out.println("Invalid user ID format: " + id);
            return false;
        } catch (Exception e) {
            throw new Exception("Error checking if user exists: " + e.getMessage());
        }
    }
    public List<Document> getTopKMostCommentedPostsOfUser(String username, int k) throws Exception {

        // Get all posts by the user
        PostsManager postsManager = new PostsManager(database);
        List<Document> userPosts = postsManager.getAllPostsByUsername(username);

        // Create a map to store post ID and number of comments
        Map<String, Integer> postCommentCount = new HashMap<>();

        // Loop through all the user's posts and count the number of comments for each post
        CommentsManager commentsManager = new CommentsManager(database);
        for(Document post : userPosts) {
            List<Document> comments = commentsManager.getAllCommentsByPostId(post.getObjectId("_id").toString());
            postCommentCount.put(post.getObjectId("_id").toString(), comments.size());
        }

        // Sort the posts by number of comments in descending order
        List<Map.Entry<String, Integer>> sortedPosts = new ArrayList<>(postCommentCount.entrySet());
        sortedPosts.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

        // Get the top K posts
        List<Document> topKPosts = new ArrayList<>();
        for(int i = 0; i < Math.min(k, sortedPosts.size()); i++) {
            String postId = sortedPosts.get(i).getKey();
            Document post = postsManager.getPostById(postId);
            if(post != null) {
                topKPosts.add(post);
            }
        }

        return topKPosts;
    }



    public List<Document> getAllUsers() throws Exception {
        try {
            return usersCollection.find().into(new ArrayList<>());
        } catch (Exception e) {
            throw new Exception("Error getting all posts: " + e.getMessage());
        }
    }
    public String getUsernameById(String userId) {
        Document query = new Document("_id", new ObjectId(userId));
        Document userDoc = usersCollection.find(query).first();
        if (userDoc == null) {
            return null;
        } else {
            return userDoc.getString("username");
        }
    }


}
