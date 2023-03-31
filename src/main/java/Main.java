import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void generateUserData(UsersManager usersManager) {
        String[] firstNames = {"John", "Sarah", "Michael", "Emily", "William", "Olivia", "Emma", "Daniel", "Sophia", "David", "Mia", "Ethan", "Isabella", "Ava", "Jacob", "Charlotte", "Amelia", "Elijah", "Abigail", "Harper"};
        String[] lastNames = {"Smith", "Johnson", "Brown", "Taylor", "Miller", "Davis", "Wilson", "Moore", "Anderson", "Jackson", "Perez", "Lee", "Garcia", "Martinez", "Lopez", "Harris", "Clark", "Lewis", "Robinson", "Walker"};

        for(int i = 0; i < 20 ; i++){
            Random random = new Random();
            String firstName = firstNames[random.nextInt(firstNames.length)];
            String lastName = lastNames[random.nextInt(lastNames.length)];
            String username = firstName + lastName + random.nextInt(100);
            String email = username.toLowerCase() + "@example.com";
            String password = "password123";

            try {
                usersManager.createUser(username, email, password);
                System.out.println("User added successfully! Username: " + username);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

    }

    public static void generateRandomFriendship(UsersManager usersManager, FriendshipsManager friendshipsManager) throws Exception {
        // Get all the users from the database
        List<Document> users = usersManager.getAllUsers();

        for(int i = 0 ; i < 20 ; i++){
            // Choose two random users
            Random rand = new Random();
            int index1 = rand.nextInt(users.size());
            int index2 = rand.nextInt(users.size());
            while (index1 == index2) {
                index2 = rand.nextInt(users.size());
            }
            String user1 = users.get(index1).getString("username");
            String user2 = users.get(index2).getString("username");

            // Create the friendship
            try {
                friendshipsManager.createFriendshipWithUsername(user1, user2);
                System.out.println("Friendship added between " + user1 + " and " + user2 + "!");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public static String generateTopicName() {
        String[] topics = {"Sports", "Technology", "Entertainment", "Food", "Travel", "Fashion", "Politics", "Education", "Business", "Science"};
        String[] adjectives = {"Awesome", "Cool", "Exciting", "Fascinating", "Interesting", "Amazing", "Incredible", "Fantastic", "Impressive", "Unbelievable"};
        String[] nouns = {"Ideas", "Facts", "News", "Updates", "Tips", "Tricks", "Strategies", "Insights", "Guides", "Trends"};
        Random rand = new Random();
        return adjectives[rand.nextInt(adjectives.length)] + " " + topics[rand.nextInt(topics.length)] + " " + nouns[rand.nextInt(nouns.length)];
    }

    public static String generateTopicDescription() {
        String[] descriptions = {"Discuss the latest news and trends in %s.", "Share your thoughts and ideas about %s.", "Get tips and tricks on how to %s better.", "Learn about the history and culture of %s.", "Connect with other people who love %s."};
        String[] topics = {"sports", "technology", "entertainment", "food", "travel", "fashion", "politics", "education", "business", "science"};
        Random rand = new Random();
        String topic = topics[rand.nextInt(topics.length)];
        String description = descriptions[rand.nextInt(descriptions.length)];
        return String.format(description, topic);
    }

    public static void generateRandomTopics(UsersManager usersManager, TopicsManager topicsManager) throws Exception {
        // Get all the users from the database
        List<Document> users = usersManager.getAllUsers();

        for(int i = 0 ; i < 20 ; i++){
            // Choose two random users
            Random rand = new Random();
            int index = rand.nextInt(users.size());

            // create random Topics
            try {
                topicsManager.createTopic(generateTopicName(), generateTopicDescription() , users.get(index).getObjectId("_id").toString());
                System.out.println("Topic added successfully!");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public static String generateRandomTitle() {
        // list of potential first halves of the title
        String[] firstHalves = {"Why I love", "My favorite", "A day in my", "The best", "The worst", "Why you should try", "The benefits of", "The importance of",
                "How to", "The future of", "The history of", "The mysteries of", "The science behind", "The art of", "The beauty of",
                "The power of", "The truth about", "The myths of", "The legends of", "The magic of", "The secrets of", "The wonders of",
                "The joy of", "The pain of", "The struggles of", "The triumphs of", "The challenges of", "The victories of", "The lessons from",
                "The adventures of", "The discoveries of", "The journeys of", "The experiences of", "The insights into", "The reflections on",
                "The musings on", "The observations of", "The contemplations on", "The imaginations of", "The dreams of", "The fantasies of",
                "The inspirations from", "The influences of", "The impacts of", "The effects of", "The causes of", "The solutions to"};

        // list of potential second halves of the title
        String[] secondHalves = {"programming", "hobby", "life", "vacation", "book", "movie", "restaurant", "meditation",
                "music", "food", "travel", "culture", "history", "science", "technology", "nature",
                "relationships", "self-improvement", "health", "fitness", "sports", "finance", "business", "career",
                "education", "politics", "society", "culture", "environment", "philosophy", "religion", "spirituality",
                "art", "literature", "writing", "creativity", "fashion", "beauty", "design", "architecture",
                "entertainment", "pop culture", "social media", "marketing", "advertising", "public relations", "communication",
                "psychology", "behavior", "neuroscience", "mindfulness", "mindset", "motivation", "inspiration", "leadership",
                "innovation", "creativity", "entrepreneurship", "startups", "investing", "trading", "economics", "globalization"};

        Random rand = new Random();
        int firstIndex = rand.nextInt(firstHalves.length);
        int secondIndex = rand.nextInt(secondHalves.length);

        String title = firstHalves[firstIndex] + " " + secondHalves[secondIndex];
        return title;
    }


    public static String generateRandomContent() {

        // list of potential post contents
        String[] contents = {"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam vel libero sed metus tempus faucibus. Nulla vel consequat turpis.",
                "Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Ut bibendum dui a libero rhoncus volutpat. ",
                "Donec ut bibendum arcu. Proin in velit vel ex maximus tincidunt sed nec enim. Donec rutrum interdum est, sed fermentum massa. ",
                "Suspendisse nec nibh quis nulla laoreet semper quis vel quam. Ut tincidunt, lorem non interdum venenatis, mauris eros volutpat mauris, a dictum mauris nulla id erat.",
                "Quisque sollicitudin elementum dignissim. Etiam iaculis sapien vitae tortor suscipit rhoncus. Fusce et mauris metus. Morbi malesuada aliquam nulla in ullamcorper.",
                "Curabitur euismod posuere magna, ac tristique mi iaculis in. Praesent lobortis gravida nulla. Suspendisse potenti. Phasellus malesuada convallis lorem, euismod iaculis odio interdum vel.",
                "Duis venenatis consectetur ex, vitae molestie nibh cursus vel. Nullam euismod dolor ut enim aliquam consequat. Praesent posuere odio eget dui posuere, id bibendum felis dapibus. ",
                "Nam ac magna mollis, feugiat justo vitae, iaculis orci. Praesent in purus ullamcorper, aliquet ex vel, ultrices turpis. Duis ac nunc aliquam, vestibulum ex ac, euismod velit."};

        Random rand = new Random();
        int contentIndex = rand.nextInt(contents.length);

        String content = contents[contentIndex];


        return content;
    }

    public static Date generateRandomDate() {
        // get the current time
        Instant now = Instant.now();

        // get the time 48 hours ago
        Instant fortyEightHoursAgo = now.minus(48, ChronoUnit.HOURS);

        // generate a random long between the two instants
        long randomEpochMilli = ThreadLocalRandom.current().nextLong(fortyEightHoursAgo.toEpochMilli(), now.toEpochMilli());

        // create a Date object from the random epoch millisecond value
        return new Date(randomEpochMilli);
    }

    public static void generateRandomPosts(UsersManager usersManager, TopicsManager topicsManager, PostsManager postsManager) throws Exception {
        List<Document> users = usersManager.getAllUsers();
        List<Document> topics = topicsManager.getAllTopics();
        for(int i = 0 ; i < 20 ; i++){
            // Choose two random users
            Random rand = new Random();
            int index_user = rand.nextInt(users.size());
            int index_topic = rand.nextInt(topics.size());
            try {
                postsManager.createPost(generateRandomTitle(), generateRandomContent(), topics.get(index_topic).getString("title"), users.get(index_user).getObjectId("_id").toString(),users.get(index_user).getString("username"), generateRandomDate());
                System.out.println("Post added successfully!");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

        }

    }

    public static void generateRandomComment(PostsManager postsManager, UsersManager usersManager, CommentsManager commentsManager) throws Exception {
        // get a list of all posts
        List<Document> posts = postsManager.getAllPosts();
        for(int i = 0; i < 20 ; i++)
        {

            if (posts.size() == 0) {
                System.out.println("There are no posts to comment on.");
                return;
            }

            Random rand = new Random();
            // choose a random post to comment on
            int postIndex = rand.nextInt(posts.size());
            Document post = posts.get(postIndex);
            String postTitle = post.getString("title");

            // choose a random user to make the comment
            List<Document> users = usersManager.getAllUsers();
            int index_user = rand.nextInt(users.size());
            String commenter = users.get(index_user).getString("username");

            // generate a random comment
            String[] commentStarts = {"I completely agree with you! ", "That's an interesting point. ", "I'm not sure I agree with you. ", "Thanks for sharing your thoughts. ", "I had a similar experience. ", "I think you're overlooking an important factor. ", "From my perspective, ", "In my opinion, ", "It's great that you mentioned ", "I disagree with ", "I strongly believe that ", "One thing I'd like to add is ", "You make a valid point. ", "I can relate to ", "It's interesting to see that ", "I've never thought about it that way. ", "What I find intriguing is ", "One thing that comes to mind is ", "I'm impressed by ", "I've been meaning to say that "};
            String[] commentEnds = {"What do you think?", "Do you have any more examples?", "I'd love to hear more about your perspective.", "Can you elaborate on that?", "Have you considered other viewpoints?", "Just my two cents.", "I completely understand where you're coming from.", "I couldn't have said it better myself.", "It's good to see different opinions on this.", "Thanks for sharing!", "I think you're on the right track.", "You've given me something to think about.", "I appreciate your honesty.", "It's important to keep an open mind.", "We need more discussions like this.", "I agree with you on this one.", "I think this is a topic that needs more attention.", "It's a complicated issue, isn't it?", "I'm looking forward to more conversations with you.", "I hope we can continue this conversation."};

            int startIndex = rand.nextInt(commentStarts.length);
            int endIndex = rand.nextInt(commentEnds.length);
            String commentContent = commentStarts[startIndex] + commentEnds[endIndex];

            try {
                commentsManager.createComment(usersManager.getUserId(commenter), post.getObjectId("_id").toString(), commentContent);
                System.out.println("Comment created successfully:");
                System.out.println(commenter + " commented on " + postTitle + ": " + commentContent);
            } catch (Exception e) {
                System.out.println("Error creating comment: " + e.getMessage());
            }
        }
    }
    public static void upvoteRandomPosts(int numPosts, PostsManager postsManager) throws Exception {
        // get a list of all posts
        List<Document> posts = postsManager.getAllPosts();

        if(posts.size() == 0) {
            System.out.println("There are no posts to upvote.");
            return;
        }

        Random rand = new Random();
        int count = 0;
        while(count < numPosts) {
            // choose a random post to upvote
            int postIndex = rand.nextInt(posts.size());
            Document post = posts.get(postIndex);

            try {
                // upvote the post
                postsManager.upVoteAPost(post.getObjectId("_id").toString());
                System.out.println("Post upvoted successfully: " + post.getString("title"));
                count++;
            } catch (Exception e) {
                System.out.println("Error upvoting post: " + e.getMessage());
            }
        }
    }



    public static void main(String[] args) throws Exception {


        // Connect to MongoDB server
        try {
            MongoClient mongoClient = new MongoClient("localhost", 27017);
            MongoDatabase database = mongoClient.getDatabase("mydb");


            // Create the various manager instances
            UsersManager usersManager = new UsersManager(database);
            FriendshipsManager friendshipsManager = new FriendshipsManager(database);
            TopicsManager topicsManager = new TopicsManager(database);
            PostsManager postsManager = new PostsManager(database);
            CommentsManager commentsManager = new CommentsManager(database);

            // Display the menu options to the user

            while (true) {
                System.out.println("Menu:");
                System.out.println("1. Add a user");
                System.out.println("2. Add a friendship");
                System.out.println("3. Add a topic");
                System.out.println("4. Add a post");
                System.out.println("5. Add a comment");
                System.out.println("6. upVote a post");
                System.out.println("7. All posts of a user");
                System.out.println("8. Top k most liked posts of a user");
                System.out.println("9. Top k most commented posts of a user");
                System.out.println("10. All posts on a topic");
                System.out.println("11. Top k most popular topics in terms of posts");
                System.out.println("12. Posts of all friends in last 24 hours");
                System.out.println("13. Exit");

                // Get the user's choice
                System.out.println("Enter your choice: ");
                int choice = Integer.parseInt(scanner.nextLine());

                // Handle the user's choice
                switch (choice) {
                    case 1:
                        generateUserData(usersManager);
                        /*System.out.println("Enter the username: ");
                        String name = scanner.nextLine();
                        System.out.println("Enter the user's email: ");
                        String email = scanner.nextLine();
                        System.out.println("Enter the user's password: ");
                        String password = scanner.nextLine();
                        try {
                            usersManager.createUser(name, email, password);
                            System.out.println("User added successfully!");
                        } catch (Exception e) {
                            System.out.println("Error: " + e.getMessage());
                        }*/
                        break;
                    case 2:

                        generateRandomFriendship(usersManager,friendshipsManager);
                        /*System.out.println("Enter the username of the first user: ");
                        String username1 = scanner.nextLine();
                        System.out.println("Enter the username of the second user: ");
                        String username2 = scanner.nextLine();
                        try {
                            friendshipsManager.createFriendshipWithUsername(username1, username2);
                            System.out.println("Friendship added successfully!");
                        } catch (Exception e) {
                            System.out.println("Error: " + e.getMessage());
                        }*/
                        break;
                    case 3:
                        generateRandomTopics(usersManager, topicsManager);
                        /*System.out.println("Enter the topic's name: ");
                        String topicName = scanner.nextLine();
                        System.out.println("Enter the topic's description: ");
                        String topicDescription = scanner.nextLine();
                        System.out.println("Enter your username: ");
                        String username = scanner.nextLine();
                        if(usersManager.userExists(username)){
                            String user_id = usersManager.getUserByUsername(username).getObjectId("_id").toString();
                            try {
                                topicsManager.createTopic(topicName, topicDescription, user_id);
                                System.out.println("Topic added successfully!");
                            } catch (Exception e) {
                                System.out.println("Error: " + e.getMessage());
                            }
                        }
                        else{
                            System.out.println("username doesnt exist");
                        }*/
                        break;
                    case 4:
                        generateRandomPosts(usersManager, topicsManager, postsManager);
                        /*System.out.println("Enter your username: ");
                        String creator = scanner.nextLine();
                        System.out.println("Enter the topic that the post belongs to: ");
                        String topic = scanner.nextLine();
                        System.out.println("Enter the Title of your Post");
                        String title = scanner.nextLine();
                        System.out.println("Enter the post's content: ");
                        String postContent = scanner.nextLine();
                        Instant instant = Instant.now();
                        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
                        Date date = Date.from(zonedDateTime.toInstant());
                        if(usersManager.userExists(creator) && topicsManager.topicExists(topic)){
                            try {
                                postsManager.createPost(title, postContent, topic, usersManager.getUserId(creator),creator, date);
                                System.out.println("Post added successfully!");
                            } catch (Exception e) {
                                System.out.println("Error: " + e.getMessage());
                            }
                        }*/

                        break;
                    case 5:
                        generateRandomComment(postsManager, usersManager, commentsManager);
                        /*try {
                            System.out.println("Enter your username: ");
                            String commenter = scanner.nextLine();
                            System.out.println("Enter post title: ");
                            String post_title = scanner.nextLine();
                            System.out.println("Enter the comment's content: ");
                            String comment = scanner.nextLine();
                            if(usersManager.userExists(commenter) && postsManager.postExists(post_title)){
                                commentsManager = new CommentsManager(database);
                                Document newComment = commentsManager.createComment(usersManager.getUserId(commenter), postsManager.getPostByTitle(post_title).getObjectId("_id").toString(), comment);
                                System.out.println("Comment created successfully:");
                                System.out.println(newComment.toJson());
                            }

                        } catch (Exception e) {
                            System.out.println("Error creating comment: " + e.getMessage());
                        }*/
                        break;
                    case 6:

                        upvoteRandomPosts(5000, postsManager);

                        /*System.out.println("Enter the post title");
                        String post_title = scanner.nextLine();
                        postsManager.upVoteAPost(postsManager.getPostByTitle(post_title).getObjectId("_id").toString());
                        */
                        break;
                    case 7:
                        System.out.println("Enter the username: ");
                        String username = scanner.nextLine();
                        List<Document> posts = postsManager.getAllPostsByUsername(username);
                        if(posts.size() == 0) {
                            System.out.println("No posts found for user " + username);
                        } else {
                            System.out.println("Posts by " + username + ":");
                            for(Document post : posts) {
                                System.out.println("Topic: " + post.getString("topicname"));
                                System.out.println("Title: " + post.getString("title"));
                                System.out.println("Content: " + post.getString("content"));
                                System.out.println("UpVotes: " + post.getInteger("upvotes"));
                                System.out.println("DownVotes: " + post.getInteger("downvotes"));
                                System.out.println("---------------");
                            }
                        }

                        break;

                    case 8:
                        System.out.println("Enter the username: ");
                        String user = scanner.nextLine();
                        System.out.println("Enter the k value: ");
                        int k = Integer.parseInt(scanner.nextLine());
                        List<Document>topkposts = postsManager.getTopKMostUpvotedPostsOfUser(user,k);
                        if(topkposts.size() == 0) {
                            System.out.println("No posts found for user " + user);
                        } else {
                            System.out.println("Top " + k +" Posts by " + user + ":");
                            for(Document post : topkposts) {
                                System.out.println("Topic: " + post.getString("topicname"));
                                System.out.println("Title: " + post.getString("title"));
                                System.out.println("Content: " + post.getString("content"));
                                System.out.println("UpVotes: " + post.getInteger("upvotes"));
                                System.out.println("DownVotes: " + post.getInteger("downvotes"));
                                System.out.println("---------------");
                            }
                        }

                        break;
                    case 9:
                        System.out.println("Enter the username: ");
                        String user1 = scanner.nextLine();
                        System.out.println("Enter the k value: ");
                        int k1 = Integer.parseInt(scanner.nextLine());
                        List<Document>topkposts1 = usersManager.getTopKMostCommentedPostsOfUser(user1,k1);
                        if(topkposts1.size() == 0) {
                            System.out.println("No posts found for user " + user1);
                        } else {
                            System.out.println("Top " + k1 +" Posts by " + user1 + ":");
                            for(Document post : topkposts1) {
                                System.out.println("Topic: " + post.getString("topicname"));
                                System.out.println("Title: " + post.getString("title"));
                                System.out.println("Content: " + post.getString("content"));
                                System.out.println("UpVotes: " + post.getInteger("upvotes"));
                                System.out.println("DownVotes: " + post.getInteger("downvotes"));
                                System.out.println("Comments: ");
                                List<Document> comments = commentsManager.getAllCommentsByPostId(post.getObjectId("_id").toString());
                                usersManager = new UsersManager(database);
                                for(Document comment : comments) {

                                    String commenter_name= usersManager.getUsernameById(comment.getObjectId("commenterId").toString());
                                    String content = comment.getString("comment");
                                    System.out.println(commenter_name + ": " + content);
                                }
                                //System.out.println(comments);
                                System.out.println("---------------");
                            }
                        }
                        break;

                    case 10:
                        //All posts on a topic
                        System.out.println("Enter a Topic:");
                        String topic1 = scanner.nextLine();
                        posts = postsManager.getAllPostsByTopic(topic1);
                        System.out.println("Posts with Topic " + topic1 + ":");
                        for(Document post : posts) {
                            //System.out.println("Topic: " + post.getString("topicname"));
                            System.out.println("Title: " + post.getString("title"));
                            System.out.println("Content: " + post.getString("content"));
                            System.out.println("UpVotes: " + post.getInteger("upvotes"));
                            System.out.println("DownVotes: " + post.getInteger("downvotes"));
                            System.out.println("---------------");
                        }
                        break;

                    case 11:
                        //Top k most popular topics in terms of posts
                        System.out.println("Enter k value:");
                        String k2 = scanner.nextLine();
                        posts = postsManager.getTopKTopicsWithMostPosts(Integer.parseInt(k2));
                        for(Document post : posts) {
                            System.out.println("title: " + post.getString("title"));
                            System.out.println("postCount: " + post.getInteger("postCount"));
                            System.out.println("---------------");
                        }
                        break;


                    case 12:
                        //Posts of all friends in last 24 hours
                        System.out.println("Enter Your Username:");
                        username = scanner.nextLine();
                        posts = postsManager.getRecentPostsOfFriends(usersManager.getUserId(username));
                        System.out.println(posts);
                        if(posts.size() == 0) {
                            System.out.println("No posts found for user " + username);
                        } else {
                            System.out.println("Posts by " + username + "'s Friends in the last 24 hrs:");
                            for(Document post : posts) {
                                System.out.println("Topic: " + post.getString("topicname"));
                                System.out.println("Title: " + post.getString("title"));
                                System.out.println("Content: " + post.getString("content"));
                                System.out.println("UpVotes: " + post.getInteger("upvotes"));
                                System.out.println("DownVotes: " + post.getInteger("downvotes"));
                                System.out.println("---------------");
                            }
                        }

                        break;
                    case 13:
                        mongoClient.close();
                        return;
                }
            }


        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
