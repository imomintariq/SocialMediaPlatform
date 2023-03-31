import com.mongodb.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FriendshipsManager {

    private MongoCollection<Document> friendshipsCollection;
    private final MongoDatabase database;

    public FriendshipsManager(MongoDatabase database) {
        this.database = database;
        this.friendshipsCollection = database.getCollection("friendships");
        createIndexes();
    }
    public boolean friendshipExists(String user1, String user2) {
        Bson filter = Filters.or(
                Filters.and(Filters.eq("user1", user1), Filters.eq("user2", user2)),
                Filters.and(Filters.eq("user1", user2), Filters.eq("user2", user1))
        );
        return friendshipsCollection.countDocuments(filter) > 0;
    }
    public void createFriendshipById(String user1Id, String user2Id) throws Exception {
        try {
            UsersManager usersManager = new UsersManager(database);

            if (usersManager.userIdExists(user1Id) && usersManager.userIdExists(user2Id)) {
                System.out.println("Both users exist");
                if (!friendshipExistsById(user1Id, user2Id)) {
                    Document newFriendship = new Document("user1", user1Id).append("user2", user2Id);
                    friendshipsCollection.insertOne(newFriendship);
                    System.out.println("Friendship created successfully");
                } else {
                    System.out.println("Friendship already exists");
                }
            } else {
                System.out.println("One or both users don't exist");
            }
        } catch (Exception e) {
            throw new Exception("Error creating friendship: " + e.getMessage());
        }
    }

    private boolean friendshipExistsById(String user1Id, String user2Id) throws Exception {
        try {
            ObjectId userId1 = new ObjectId(user1Id);
            ObjectId userId2 = new ObjectId(user2Id);
            Document query1 = new Document("user1", userId1).append("user2", userId2);
            Document query2 = new Document("user1", userId2).append("user2", userId1);
            long count1 = friendshipsCollection.countDocuments(query1);
            long count2 = friendshipsCollection.countDocuments(query2);
            return (count1 > 0) || (count2 > 0);
        } catch (IllegalArgumentException e) {
            // Handle the case where either user1Id or user2Id is not a valid ObjectId
            System.out.println("Invalid user ID format: " + e.getMessage());
            return false;
        } catch (Exception e) {
            throw new Exception("Error checking if friendship exists: " + e.getMessage());
        }
    }
    public List<Document> getAllFriendships() throws Exception {
        try {
            return friendshipsCollection.find().into(new ArrayList<>());
        } catch (Exception e) {
            throw new Exception("Error getting all topics: " + e.getMessage());
        }
    }

    public List<Document> getAllFriendshipsByUser(String userId) throws Exception {
        try {
            List<Document> friendshipsList = new ArrayList<>();

            // Retrieve all the friendships that have user as one of the parties
            List<Document> allFriendships = getAllFriendships();
            for (Document friendship : allFriendships) {

                String user1 = friendship.get("user1").toString();
                String user2 = friendship.get("user2").toString();
                if (user1.equals(userId) || user2.equals(userId)) {
                    friendshipsList.add(friendship);
                }
            }

            return friendshipsList;

        } catch (Exception e) {
            throw new Exception("Error retrieving friendships for user with ID " + userId + ": " + e.getMessage());
        }
    }


    public void createFriendship( String user1, String user2) throws Exception {
        try {

            UsersManager usersManager = new UsersManager(database);
            if(usersManager.userIdExists(user1) == true && usersManager.userIdExists(user2) == true) {
                System.out.println("They Exist");
                if (friendshipExists(user1, user2) != true) {
                    Document newFriendship = new Document("user1", user1).append("user2", user2);
                    friendshipsCollection.insertOne(newFriendship);
                } else {
                    System.out.println("Friendship Alredy Exists");
                }
            }
            else{
                System.out.println("User 1 or 2 doesnt exist");
            }

        } catch (Exception e) {
            throw new Exception("Error creating friendship: " + e.getMessage());
        }
    }

    public Document getFriendshipById(String id) throws Exception {
        try {
            ObjectId objectId = new ObjectId(id);
            Document query = new Document("_id", objectId);
            Document friendshipById = friendshipsCollection.find(query).first();
            if (friendshipById != null) {
                return friendshipById;
            } else {
                System.out.println("Error: Friendship with id " + id + " doesn't exist...");
                throw new MongoException("Error: Friendship with id " + id + " doesn't exist...");
            }

        } catch (Exception e) {
            throw new Exception("Error getting friendship by ID: " + e.getMessage());
        }
    }



    public Document getFriendshipByUsers(String user1, String user2) throws Exception {
        try {
            Bson query = Filters.or(
                    Filters.and(Filters.eq("user1", user1), Filters.eq("user2", user2)),
                    Filters.and(Filters.eq("user1", user2), Filters.eq("user2", user1))
            );
            return friendshipsCollection.find(query).first();
        } catch (Exception e) {
            throw new Exception("Error getting friendship by users: " + e.getMessage());
        }
    }



    public void deleteFriendship(String id) throws Exception {
        try {
            ObjectId objectId = new ObjectId(id);
            Document query = new Document("_id", objectId);
            friendshipsCollection.deleteOne(query);
        } catch (Exception e) {
            throw new Exception("Error deleting friendship: " + e.getMessage());
        }
    }
    public void deleteFriendshipBetweenUsers(String user1, String user2) throws Exception {
        try {
            Document query1 = new Document("user1", user1).append("user2", user2);
            Document query2 = new Document("user1", user2).append("user2", user1);
            if (!friendshipExists(user1, user2)) {
                throw new Exception("Friendship does not exist.");
            }
            friendshipsCollection.deleteOne(query1);
            friendshipsCollection.deleteOne(query2);
        } catch (Exception e) {
            throw new Exception("Error deleting friendship: " + e.getMessage());
        }
    }



    private void createIndexes() {
        try {
            friendshipsCollection.createIndex(
                    Indexes.ascending("user1", "user2"),
                    new IndexOptions().unique(true));
        } catch (MongoCommandException e) {
            System.out.println("Error creating index: " + e.getErrorMessage());
        }
    }

    public void shardCollection() {
        try {
            Document shardKey = new Document("_id", "hashed");
            friendshipsCollection.createIndex(shardKey);
            Document command = new Document("shardCollection", friendshipsCollection.getNamespace().toString())
                    .append("key", shardKey);
            MongoClient mongoClient = new MongoClient("localhost", 27017);
            mongoClient.getDatabase("admin").runCommand(command);
            System.out.println("Collection sharded.");
        } catch (Exception e) {
            System.out.println("Error sharding collection: " + e.getMessage());
        }
    }

    public void createFriendshipWithUsername(String username1, String username2) throws Exception {
        try {
            // Check if both users exist
            UsersManager userManager = new UsersManager(database);
            Document user1 = userManager.getUserByUsername(username1);
            Document user2 = userManager.getUserByUsername(username2);
            if (user1 == null) {
                throw new Exception("Error creating friendship: User with username " + username1 + " doesn't exist...");
            } else if (user2 == null) {
                throw new Exception("Error creating friendship: User with username " + username2 + " doesn't exist...");
            } else {
                createFriendship(user1.getObjectId("_id").toString(), user2.getObjectId("_id").toString());
                System.out.println("Friendship created between " + username1 + " and " + username2 + ".");
            }
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid username format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error creating friendship: " + e.getMessage());
        }
    }

}
