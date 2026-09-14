## Funtional Requirement
 - Type of notifcation (push, in-App, sms,  email) --> InApp, push and email
 - Type if delivery --> one to many, brodcast, one to one, and feature based
 - Retry logic - Yes and no, depends on the feature
 - latency - depends on the feature, support both
 - Notification history required - Yes required
 - Read/Unread support - Yes


## NonFunctional Requirement
 1. Expected TPS
 ``` 
  users count approx  - 15k
  notification per day for user - 50/day
   
   15k * 50  = 750k req/day
   on avg - 18 req/sec
   on Peak - 100 to 200 req/sec

   Conclusion - 
   - Single db can work here but artitecture support futural scaling.

 ```

# HLD 

[Client App]  -- APIs ---> [notification service] -----> [Message Queue]  ------> [worker/consumer]  -- sms/email/push ---> [notification provider](firebase/twilio)


## DB selection and tables design
 Since this is the structure data for notification and user prefrence and everyt ime we have fetch the info like no. of unread count, how many notifcation not deliver etc so i prefer structure database that is postgress. 

### Table design
 Since we have to persist the every notification and also we have to use the user prefrence like whihc user have what prefrence and last table is notification delivery here this track each notification support multiple delivery chnalle and what is the status of that notifications. 

 Q. Why I'd select PostgreSQL? 
  - I'd choose PostgreSQL because our notification data is structured and relational, and we need transactional consistency for operations such as creating notifications and updating delivery/read state. Our primary access patterns—fetching recent notifications by user, querying unread notifications, and updating notification status—map very naturally to indexed SQL queries. At 15k users, PostgreSQL is more than sufficient and keeps the architecture simple. MySQL would also be a valid choice; the selection between them is largely based on team expertise and specific database features. I wouldn't choose MongoDB unless our data model became significantly more document-oriented or schema-flexible 

 Q. Why not just use MongoDB because it scales horizontally?
 - Horizontal scalability isn't the only criterion. We should choose the database based on our access patterns and consistency requirements. At our current scale, PostgreSQL gives us simpler operations and strong relational querying without needing the additional complexity of a distributed NoSQL system



## Notification Flow 
1. Client App (producer)  - Here the notification produce by either someone create notifcation or some servoce notification we have to deliver. So this service create an notifcation by calling the notfication service apis. 
 ```json
 POST /v1/api/notify 
 ```
 payload: 
 ```json 
 {
  "userId": 101,
  "type": "ORDER_SUCCESS"
 }
 ```

2. Notification Service 
 - validate request
 - check user prefrence
 - apply business logic 
 - persist the request in db
 - push event to queue

3. worker / consumer service
- worker consume the request 
- fetch the original request form the db
- calls provider 
- update the table based on the status

4. Retry stratergy
- 1 st retry in 1 min
- 2nd retry in 15 min
- 3rd retry in 30 min
- then move to DLQ



## Follow-Up Interview Questions I Would Ask You
1. How would you guarantee notification ordering?
2. How would you avoid duplicate notifications?
3. How would you design real-time websocket notifications?
4. How would you support scheduled notifications?
5. How would you scale broadcast notifications to millions?
6. What happens if queue crashes?
7. Why Kafka vs RabbitMQ?
8. How would you design notification templates?
9. How would you support user timezone delivery?
10. How would you design unread count efficiently?