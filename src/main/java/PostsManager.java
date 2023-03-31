import com.mongodb.MongoException;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import javax.annotation.processing.SupportedSourceVersion;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

public class PostsManager {

    private final MongoCollection<Document> postsCollection;
    private final MongoDatabase database;

    public PostsManager(MongoDatabase database) {
        this.database = database;
        this.postsCollection = database.getCollection("posts");
        createIndexes();
    }

    private void createIndexes() {
        // Create index on the "title" field, with unique values and in ascending order
        postsCollection.createIndex(Indexes.ascending("title"), new IndexOptions().unique(true));
    }



    public Document createPost(String title, String content, String topicName, String creatorId, String username, Date creationDate) throws Exception {
        try {
            TopicsManager topicsManager = new TopicsManager(database);
            UsersManager usersManager = new UsersManager(database);
            if (!topicsManager.topicExists(topicName)) {
                throw new Exception("Error creating post: Topic " + topicName + " doesn't exist...");
            } else {
                Document newPost = new Document("title", title)
                        .append("content", content)
                        .append("topicid", topicsManager.getTopicByName(topicName).getObjectId("_id"))
                        .append("topicname", topicName)
                        .append("creatorId", new ObjectId(creatorId))
                        .append("creatorUsername", username)
                        .append("creationDate", creationDate)
                        .append("upvotes", 0)
                        .append("downvotes", 0);

                postsCollection.insertOne(newPost);

                topicsManager.incrementPostCount(topicsManager.getTopicByName(topicName).getObjectId("_id").toString());
                return newPost;
            }
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid creator ID or topic ID format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error creating post: " + e.getMessage());
        }
    }
    public boolean postExists(String postTitle) {
        try {
            Document post = postsCollection.find(Filters.eq("title", postTitle)).first();
            return post != null;
        } catch (Exception e) {
            System.err.println("Error checking post existence: " + e);
            return false;
        }
    }
    public Document getPostByTitle(String title) throws Exception {
        try {
            Document post = postsCollection.find(Filters.eq("title", title)).first();
            if (post == null) {
                throw new Exception("Post with title " + title + " doesn't exist...");
            } else {
                return post;
            }
        } catch (Exception e) {
            throw new Exception("Error getting post: " + e.getMessage());
        }
    }



    public List<Document> getAllPostsByUsername(String username) throws Exception {
        try {
            Bson filter = Filters.eq("creatorUsername", username);
            return postsCollection.find(filter).into(new ArrayList<>());
        } catch (Exception e) {
            throw new Exception("Error getting posts by username: " + e.getMessage());
        }
    }


    public List<Document> getAllPosts() throws Exception {
        try {
            return postsCollection.find().into(new ArrayList<>());
        } catch (Exception e) {
            throw new Exception("Error getting all posts: " + e.getMessage());
        }
    }

    public Document getPostById(String id) throws Exception {
        try {
            ObjectId objectId = new ObjectId(id);
            Document query = new Document("_id", objectId);
            Document postById = postsCollection.find(query).first();
            if (postById != null) {
                return postById;
            } else {
                System.out.println("Error: Post with id " + id + " doesn't exist...");
                throw new MongoException("Error: Post with id " + id + " doesn't exist...");
            }
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid post ID format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error getting post by ID: " + e.getMessage());
        }
    }

    public void deletePostByTitle(String title) throws Exception {
        Document filter = new Document("title", title);
        Document deletedPost = postsCollection.findOneAndDelete(filter);
        if (deletedPost != null) {
            TopicsManager topicsManager = new TopicsManager(database);
            topicsManager.decrementPostCount(deletedPost.getObjectId("topicid").toString());
        } else {
            System.out.println("Error: Post with title " + title + " doesn't exist...");
            throw new MongoException("Error: Post not found");
        }
    }



    public Document updatePostContent(String id, String newContent) throws Exception {
        try {
            ObjectId objectId = new ObjectId(id);
            Document filter = new Document("_id", objectId);
            Document update = new Document("$set", new Document("content", newContent));
            Document updatedPost = postsCollection.findOneAndUpdate(filter, update, new FindOneAndUpdateOptions()
                    .returnDocument(ReturnDocument.AFTER));
            if (updatedPost != null) {
                return updatedPost;
            } else {
                System.out.println("Error: Post with id " + id + " doesn't exist...");
                throw new MongoException("Error: Post with id " + id + " doesn't exist...");
            }
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid post ID format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error updating post content: " + e.getMessage());
        }

    }

    public List<Document> getAllPostsByCreator(String creatorId) throws Exception {
        try {
            ObjectId objectId = new ObjectId(creatorId);
            Bson query = Filters.eq("creatorId", objectId);
            return postsCollection.find(query).into(new ArrayList<>());
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid creator ID format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error getting all posts by creator: " + e.getMessage());
        }
    }
    public List<Document> allPostsOnTopic(String topicName) throws Exception {
        try {
            TopicsManager topicsManager = new TopicsManager(database);
            if (!topicsManager.topicExists(topicName)) {
                throw new Exception("Error fetching posts: Topic " + topicName + " doesn't exist...");
            }
            else {
                Document filter = new Document("topicname", topicName);
                List<Document> posts = postsCollection.find(filter).into(new ArrayList<>());
                return posts;
            }
        } catch (Exception e) {
            throw new Exception("Error fetching posts: " + e.getMessage());
        }
    }

    public void upVoteAPost(String postId) throws Exception {
        try {
            ObjectId objectId = new ObjectId(postId);
            Document filter = new Document("_id", objectId);
            Document update = new Document("$inc", new Document("upvotes", 1));
            postsCollection.updateOne(filter, update);

        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid post ID format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error upvoting post: " + e.getMessage());
        }
    }
    public Document downVoteAPost(String postId) throws Exception {
        try {
            ObjectId objectId = new ObjectId(postId);
            Document filter = new Document("_id", objectId);
            Document update = new Document("$inc", new Document("downvotes", 1));
            Document updatedPost = postsCollection.findOneAndUpdate(filter, update, new FindOneAndUpdateOptions()
                    .returnDocument(ReturnDocument.AFTER));
            if (updatedPost != null) {
                return updatedPost;
            } else {
                System.out.println("Error: Post with id " + postId + " doesn't exist...");
                throw new MongoException("Error: Post with id " + postId + " doesn't exist...");
            }
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid post ID format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error downvoting post: " + e.getMessage());
        }
    }
    public List<Document> getTopKMostUpvotedPostsOfUser(String username, int k) throws Exception {
        try {
            UsersManager usersManager = new UsersManager(database);
            ObjectId userId = usersManager.getUserByUsername(username).getObjectId("_id");
            FindIterable<Document> posts = postsCollection.find(new Document("creatorId", userId));
            List<Document> sortedPosts = posts.sort(new Document("upvotes", -1)).limit(k).into(new ArrayList<>());
            return sortedPosts;
        } catch (Exception e) {
            throw new Exception("Error getting top " + k + " most upvoted posts of user " + username + ": " + e.getMessage());
        }
    }

    public List<Document> getAllPostsByTopic(String topic) throws Exception {
        PostsManager postsManager = new PostsManager(database);
        List<Document> posts = postsManager.getAllPosts();
        List<Document> postsByTopic = new ArrayList<>();

        for(Document post : posts) {
            if(post.getString("topicname").equals(topic)) {
                postsByTopic.add(post);
            }
        }

        return postsByTopic;
    }



    public List<Document> getTopKTopicsWithMostPosts(int k) throws Exception {
        try {
            TopicsManager topicsManager = new TopicsManager(database);
            List<Document> topTopics = new ArrayList<>();

            // Get all topics with their post counts
            List<Document> topicPostCounts = topicsManager.getAllTopics();
            System.out.println(topicPostCounts);
            topicPostCounts.removeIf(topic -> topic.getInteger("postCount") == null);
            // Sort the topics by post count in descending order

            topicPostCounts.sort((t1, t2) -> Integer.compare(t2.getInteger("postCount"), t1.getInteger("postCount")));

            // Add the top k topics to the result list
            for (int i = 0; i < k && i < topicPostCounts.size(); i++) {
                Document topic = topicPostCounts.get(i);
                topTopics.add(new Document("title", topic.getString("title"))
                        .append("postCount", topic.getInteger("postCount")));
            }
            System.out.println(topTopics);

            return topTopics;
        } catch (Exception e) {
            throw new Exception("Error getting top " + k + " topics: " + e.getMessage());
        }
    }


    public List<Document> getAllFriendshipsOfUser(String userId) throws Exception {
        try {
            List<Document> friendships = new ArrayList<>();
            FriendshipsManager friendsManager = new FriendshipsManager(database);

            // Get all friendships where the user is involved
            List<Document> userFriendships = friendsManager.getAllFriendshipsByUser(userId);
            System.out.println("Hello");
            // Add the friend information to the result list
            UsersManager usersManager = new UsersManager(database);
            for (Document friendship : userFriendships) {
                String friendId;
                if(friendship.getString("user1").equals(userId)){
                    friendId = friendship.getString("user2");
                }
                else{
                    friendId = friendship.getString("user1");
                }

                Document friend = usersManager.getUserById(friendId);

                // If the friend document exists, add it to the result list
                if (friend != null) {
                    friendships.add(new Document("friendId", friendId)
                            .append("friendUsername", friend.getString("username")));
                }
            }

            return friendships;
        } catch (Exception e) {
            throw new Exception("Error getting all friendships of user with id " + userId + ": " + e.getMessage());
        }
    }

    public List<Document> getAllPostsByUsernameFromPast24Hrs(String username) throws Exception {
        try {
            UsersManager usersManager = new UsersManager(database);
            ObjectId userId = usersManager.getUserByUsername(username).getObjectId("_id");
            if (userId == null) {
                throw new Exception("User with username " + username + " does not exist");
            }

            // get the current time and the time 24 hours ago
            Instant now = Instant.now();
            Instant twentyFourHoursAgo = now.minus(24, ChronoUnit.HOURS);

            // construct the query to find posts created in the last 24 hours by the user
            Document query = new Document();
            query.append("creatorId", userId);
            query.append("creationDate", new Document("$gte", Date.from(twentyFourHoursAgo)));

            // find the posts and sort them in descending order of creation date
            List<Document> posts = postsCollection.find(query)
                    .sort(new Document("creationDate", -1))
                    .into(new ArrayList<>());

            return posts;
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid username format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error getting posts by username from past 24 hours: " + e.getMessage());
        }
    }



    public List<Document> getRecentPostsOfFriends(String userId) throws Exception {
        try {
            // get the user document
            MongoCollection<Document> usersCollection;
            usersCollection = database.getCollection("users");
            Document user = usersCollection.find(new Document("_id", new ObjectId(userId))).first();
            if (user == null) {
                throw new Exception("User with ID " + userId + " does not exist");
            }

            // get the user's friends
            List<Document> friendIds = getAllFriendshipsOfUser(userId);
            if (friendIds.isEmpty()) {
                return Collections.emptyList();
            }
            System.out.println(friendIds);

            List<Document> posts = new ArrayList<>();
            for(Document  friend : friendIds) {

                posts.addAll(getAllPostsByUsernameFromPast24Hrs(friend.get("friendUsername").toString()));


            }

            return posts;
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid user ID format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error getting recent posts of friends: " + e.getMessage());
        }
    }


}