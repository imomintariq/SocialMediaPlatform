import com.mongodb.MongoException;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class TopicsManager {

    private final MongoCollection<Document> topicsCollection;
    private final MongoDatabase database;

    public TopicsManager(MongoDatabase database) {
        this.database = database;
        this.topicsCollection = database.getCollection("topics");
        createIndexes();
    }

    private void createIndexes() {
        // Create index on the "title" field, with unique values and in ascending order
        topicsCollection.createIndex(Indexes.ascending("title"), new IndexOptions().unique(true));
    }

    
    public Document createTopic(String title, String description, String creatorId) throws Exception {
        try {
            if(!topicExists(title)){
                Document newTopic = new Document("title", title)
                        .append("description", description)
                        .append("creatorId", new ObjectId(creatorId))
                        .append("postCount", 0);

                topicsCollection.insertOne(newTopic);
                return newTopic;
            }
            else{

                System.out.println("Topic Already exists");
                return getTopicByName(title);
            }
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid creator ID format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error creating topic: " + e.getMessage());
        }
    }

    public List<Document> getAllTopics() throws Exception {
        try {
            return topicsCollection.find().into(new ArrayList<>());
        } catch (Exception e) {
            throw new Exception("Error getting all topics: " + e.getMessage());
        }
    }

    public Document getTopicById(String id) throws Exception {
        try {
            ObjectId objectId = new ObjectId(id);
            Document query = new Document("_id", objectId);
            Document topicById = topicsCollection.find(query).first();
            if (topicById != null) {
                return topicById;
            } else {
                System.out.println("Error: Topic with id " + id + " doesn't exist...");
                throw new MongoException("Error: Topic with id " + id + " doesn't exist...");
            }
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid topic ID format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error getting topic by ID: " + e.getMessage());
        }
    }

    public Document updateTopicTitle(String id, String newTitle) throws Exception {
        try {
            ObjectId objectId = new ObjectId(id);
            Document filter = new Document("_id", objectId);
            Document update = new Document("$set", new Document("title", newTitle));
            Document updatedTopic = topicsCollection.findOneAndUpdate(filter, update, new FindOneAndUpdateOptions()
                    .returnDocument(ReturnDocument.AFTER));
            if (updatedTopic != null) {
                return updatedTopic;
            } else {
                System.out.println("Error: Topic with id " + id + " doesn't exist...");
                throw new MongoException("Error: Topic with id " + id + " doesn't exist...");
            }
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid topic ID format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error updating topic title: " + e.getMessage());
        }
    }
    public boolean topicExists(String title) throws Exception {
        try {
            Document query = new Document("title", title);
            return topicsCollection.countDocuments(query) > 0;
        } catch (Exception e) {
            throw new Exception("Error checking if topic exists: " + e.getMessage());
        }
    }

    public boolean topicExistsById(String id) throws Exception {
        try {
            ObjectId objectId = new ObjectId(id);
            Document query = new Document("_id", objectId);
            return topicsCollection.countDocuments(query) != 0;
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid topic ID format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error checking if topic exists: " + e.getMessage());
        }
    }


    public void deleteTopic(String id) throws Exception {
        try {
            ObjectId objectId = new ObjectId(id);
            Document query = new Document("_id", objectId);
            topicsCollection.deleteOne(query);
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid topic ID format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error deleting topic: " + e.getMessage());
        }
    }
    public Document getTopicByName(String name) throws Exception {
        try {
            Document query = new Document("title", name);
            Document topic = topicsCollection.find(query).first();
            if (topic != null) {
                return topic;
            } else {
                System.out.println("Error: Topic with name " + name + " doesn't exist...");
                throw new MongoException("Error: Topic with name " + name + " doesn't exist...");
            }
        } catch (Exception e) {
            throw new Exception("Error getting topic by name: " + e.getMessage());
        }
    }
    public List<Document> searchTopicByKeyword(String keyword) throws Exception {
        try {
            Document query = new Document("$or", Arrays.asList(
                    new Document("title", Pattern.compile(keyword, Pattern.CASE_INSENSITIVE)),
                    new Document("description", Pattern.compile(keyword, Pattern.CASE_INSENSITIVE))
            ));
            return topicsCollection.find(query).into(new ArrayList<>());
        } catch (Exception e) {
            throw new Exception("Error searching topics by keyword: " + e.getMessage());
        }
    }
    public void incrementPostCount(String topicId) throws Exception {
        try {
            ObjectId objectId = new ObjectId(topicId);
            Document filter = new Document("_id", objectId);
            Document update = new Document("$inc", new Document("postCount", 1));
            topicsCollection.updateOne(filter, update);
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid topic ID format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error incrementing post count: " + e.getMessage());
        }
    }

    public void decrementPostCount(String topicId) {
        try {
            ObjectId objectId = new ObjectId(topicId);
            Document filter = new Document("_id", objectId);
            Document update = new Document("$inc", new Document("postCount", -1));
            topicsCollection.updateOne(filter, update);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid topic ID format: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error decrementing post count: " + e.getMessage());
        }
    }

    public List<Document> getAllTopicsWithPostCount() throws Exception {
        try {
            List<Document> topicsWithCount = new ArrayList<>();
            AggregateIterable<Document> results = topicsCollection.aggregate(Arrays.asList(
                    new Document("$lookup", new Document("from", "posts")
                            .append("localField", "_id")
                            .append("foreignField", "topicid")
                            .append("as", "posts")),
                    new Document("$project", new Document("_id", 1)
                            .append("name", 1)
                            .append("count", new Document("$size", "$posts")))
            ));

            for (Document result : results) {
                topicsWithCount.add(result);
            }
            return topicsWithCount;
        } catch (Exception e) {
            throw new Exception("Error getting topics with post count: " + e.getMessage());
        }
    }




}
