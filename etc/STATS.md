## Linkable

* C - Dr (Most direct replies to a comment)

        SELECT SbnId, Username, Url, Count
        FROM (
          SELECT C.SbnId, U.Username, P.Url, COUNT(1) AS Count
          FROM Comment C
            JOIN Post P
              ON P.Id = C.PostId
            JOIN User U
              ON U.Id = C.UserId
            JOIN Comment CSub
              ON CSub.ParentId = C.Id
          GROUP BY C.SbnId, U.Username, P.Url
          ) C
        ORDER BY Count DESC LIMIT 3
    
* C - F (Most fuck in a comment)

        SELECT SbnId, Username, Url, SubjectCount + ContentsCount
        FROM (
          SELECT C.SbnId, U.Username, P.Url, 
            (LENGTH(C.Subject) - LENGTH(REPLACE(LOWER(C.Subject), 'fuck', ''))) / 4  AS SubjectCount,
            (LENGTH(C.Contents) - LENGTH(REPLACE(LOWER(C.Contents), 'fuck', ''))) / 4  AS ContentsCount
          FROM Comment C
            JOIN Post P
              ON P.Id = C.PostId
            JOIN User U
              ON U.Id = C.UserId
          GROUP BY C.SbnId, U.Username, P.Url
          ) C
        ORDER BY SubjectCount + ContentsCount DESC LIMIT 3

* P - F (Most fuck in all comments within a post)

        SELECT Url, Count
        FROM (
          SELECT P.Url, 
            SUM((LENGTH(C.Contents) - LENGTH(REPLACE(LOWER(C.Contents), 'fuck', '')))) / 4  AS Count
          FROM Post P
            JOIN Comment C
              ON C.PostId = P.Id
          GROUP BY P.Url
          ) P
        ORDER BY Count DESC LIMIT 3

* P - Dt (Deepest subthread in a post)

        SELECT C.SbnId, P.Url, CSub.Depth
        FROM Comment C
          JOIN Post P
            ON P.Id = C.PostId
          JOIN Comment CSub
            ON CSub.TopLevelParentId = C.Id
        ORDER BY CSub.Depth DESC LIMIT 3

* P - C (Most comments on a post)

        SELECT Url, Count
        FROM (
          SELECT P.Url, COUNT(1) AS Count
          FROM Post P
            JOIN Comment C
              ON C.PostId = P.Id
            GROUP BY P.Url
          ) C
        ORDER BY Count DESC LIMIT 3

* C - Rec (Most recs on a comment)

        SELECT C.SbnId, P.Url, C.RecommendationCount
        FROM Comment C
          JOIN Post P
            ON P.Id = C.PostId
        ORDER BY C.RecommendationCount DESC LIMIT 3

* P - Rec (Most recs on a post)

        SELECT P.Url, P.RecommendationCount
        FROM Post P
        ORDER BY P.RecommendationCount DESC LIMIT 3

* P - Lc (Latest comment on a post)

        SELECT SbnId, Username, Url, TimeDiff
        FROM (
          SELECT C.SbnId, U.Username, P.Url, C.Date - P.Date AS TimeDiff
          FROM Comment C
            JOIN Post P
              ON P.Id = C.PostId
            JOIN User U
              ON U.Id = C.UserId
          ) C
        ORDER BY TimeDiff DESC LIMIT 3

* U - Time (Longest time between commenting for a user)

        SELECT FirstSbnId, FirstUrl, LastSbnId, LastUrl, Username, TimeDiff
        FROM (
          SELECT CFirst.SbnId AS FirstSbnId, PFirst.Url AS FirstUrl, 
            CLast.SbnId AS LastSbnId, PLast.Url AS LastUrl, 
            U.Username, CLast.Date - CFirst.Date AS TimeDiff
          FROM User U
            JOIN Comment CFirst
              ON CFirst.UserId = U.Id
            JOIN Post PFirst
              ON PFirst.Id = CFirst.PostId
            JOIN Comment CLast
              ON CLast.UserId = U.Id
            JOIN Post PLast
              ON PLast.Id = CLast.PostId
          ) U
        ORDER BY TimeDiff DESC LIMIT 3
        
* C - CLen (Longest comment)

        SELECT SbnId, Username, Url, SubjectCount + ContentsCount
        FROM (
          SELECT C.SbnId, U.Username, P.Url, 
            CHAR_LENGTH(C.Subject) AS SubjectCount,
            CHAR_LENGTH(C.Contents) AS ContentsCount
          FROM Comment C
            JOIN Post P
              ON P.Id = C.PostId
            JOIN User U
              ON U.Id = C.UserId
          GROUP BY C.SbnId, U.Username, P.Url
          ) C
        ORDER BY SubjectCount + ContentsCount DESC LIMIT 3

## User Level

* U - C (Comments for a user)

        SELECT Username, Count
        FROM (
          SELECT U.Username, COUNT(1) AS Count
          FROM User U
            JOIN Comment C
              ON C.UserId = U.Id
          GROUP BY U.Username
          )U 
        ORDER BY Count DESC LIMIT 3

* U - Fp (Fan posts for a user)

        SELECT Username, Count
        FROM (
          SELECT U.Username, COUNT(1) AS Count
          FROM User U
            JOIN Post P
              ON P.UserId = U.Id
          WHERE P.Type = 0
          GROUP BY U.Username
          ) U 
        ORDER BY Count DESC LIMIT 3

* U - Fs (Fan shots for a user)

        SELECT Username, Count
        FROM (
          SELECT U.Username, COUNT(1) AS Count
          FROM User U
            JOIN Post P
              ON P.UserId = U.Id
          WHERE P.Type = 1
          GROUP BY U.Username
          ) U 
        ORDER BY Count DESC LIMIT 3

* U - Rec/C (Recs per comment for a user)

        SELECT Username, RecommendationCount / Count
        FROM (
          SELECT U.Username, COUNT(1) AS Count, 
            SUM(C.RecommendationCount) AS RecommendationCount
          FROM User U
            JOIN Comment C
              ON C.UserId = U.Id
          GROUP BY U.Username
          ) U 
        WHERE Count > 0
        ORDER BY RecommendationCount / Count DESC LIMIT 3

* U - Rec/P (Recs per post for a user)

        SELECT Username, RecommendationCount / Count
        FROM (
          SELECT U.Username, COUNT(1) AS Count, 
            SUM(P.RecommendationCount) AS RecommendationCount
          FROM User U
            JOIN Post P
              ON P.UserId = U.Id
          GROUP BY U.Username
          ) U 
        WHERE Count > 0
        ORDER BY RecommendationCount / Count DESC LIMIT 3

* U - F/C (fuck per comment for a user)

        SELECT Username, (SubjectCount + ContentsCount) / Count
        FROM (
          SELECT U.Username, COUNT(1) AS Count,
            SUM((LENGTH(C.Subject) - LENGTH(REPLACE(LOWER(C.Subject), 'fuck', ''))) / 4)  AS SubjectCount,
            SUM((LENGTH(C.Contents) - LENGTH(REPLACE(LOWER(C.Contents), 'fuck', ''))) / 4)  AS ContentsCount
          FROM User U
            JOIN Comment C
              ON C.UserId = U.Id
          GROUP BY U.Username
          ) C
        WHERE Count > 0
        ORDER BY (SubjectCount + ContentsCount) / Count DESC LIMIT 3

* U - Dr/C (Direct replies per comment for a user)

        SELECT Username, ReplyCount / Count
        FROM (
          SELECT U.Username, COUNT(1) AS Count, SUM(CSub.Count) AS ReplyCount
          FROM User U
            JOIN Comment C
              ON C.UserId = U.Id
            JOIN (
              SELECT C.Id, COUNT(1) Count
              FROM Comment C
                JOIN Comment CSub
                  ON CSub.ParentId = C.Id
              GROUP BY C.Id
              ) CSub
              ON CSub.Id = C.Id
          GROUP BY U.Username
          ) C
        WHERE Count > 0
        ORDER BY ReplyCount / Count DESC LIMIT 3
        
* U - CLen/C (Comment length per comment)

        SELECT Username, (SubjectCount + ContentsCount) / Count
        FROM (
          SELECT U.Username, COUNT(1) AS Count,
            SUM(CHAR_LENGTH(C.Subject)) AS SubjectCount,
            SUM(CHAR_LENGTH(C.Contents)) AS ContentsCount
          FROM Comment C
            JOIN User U
              ON U.Id = C.UserId
          GROUP BY U.Username
          ) C
        WHERE Count > 0
        ORDER BY (SubjectCount + ContentsCount) / Count DESC LIMIT 3

* U - C - D (Most comments in a day for a user)

        SELECT Username, Date, Count
        FROM (
          SELECT U.Username, DATE(C.Date) AS Date, COUNT(1) AS Count
          FROM Comment C
            JOIN User U
              ON U.Id = C.UserId
          GROUP BY U.Username, DATE(C.Date)
          ) C
        ORDER BY Count DESC LIMIT 3
        
## Site Level

* C (Comments)
        SELECT COUNT(1)
        FROM Comment
        
* C - Y (Comments by year)

        SELECT COUNT(1), YEAR(Date)
        FROM Comment
        GROUP BY YEAR(Date)
        
* C - Dw (Comments by day of week)

        SELECT COUNT(1), WEEKDAY(Date)
        FROM Comment
        GROUP BY WEEKDAY(Date)
        
* C - H (Comments by hour of day)

        SELECT COUNT(1), HOUR(Date)
        FROM Comment
        GROUP BY HOUR(Date)
        
* C - D (Most comments in a day)

        SELECT Date, Count
        FROM (
          SELECT DATE(C.Date) AS Date, COUNT(1) AS Count
          FROM Comment C
          GROUP BY DATE(C.Date)
          ) C
        ORDER BY Count DESC LIMIT 3
        
* P (Posts)

        SELECT COUNT(1)
        FROM Post
        
* P - Fs (Fan shots)

        SELECT COUNT(1)
        FROM Post
        WHERE Type = 1        

* P - Fp (Fan posts)

        SELECT COUNT(1)
        FROM Post
        WHERE Type = 0
        
* U (Users)
        
        SELECT COUNT(1)
        FROM User
        
* Ua (Active users)
        
        SELECT COUNT(1)
        FROM User
        WHERE Url IS NOT NULL

* Rec/C (Recs per comment)

        SELECT RecommendationCount / Count
        FROM (
          SELECT COUNT(1) AS Count, 
            SUM(C.RecommendationCount) AS RecommendationCount
          FROM Comment C
          ) C

* Rec/P (Recs per post)

        SELECT RecommendationCount / Count
        FROM (
          SELECT COUNT(1) AS Count, 
            SUM(P.RecommendationCount) AS RecommendationCount
          FROM Post P
          ) P



## Uecker

* Workday (Comments from 11 AM to 4 PM)

        SELECT Username, Count
        FROM (
          SELECT U.Username, 
            SUM(CASE WHEN TIME(C.Date) BETWEEN '11:00:00' AND '16:00:00' THEN 1 ELSE 0 END) AS Count
          FROM User U
            JOIN Comment C
              ON C.UserId = U.Id
          GROUP BY U.Username
          ) C
        ORDER BY Count DESC LIMIT 3

* Workday / C (Comments from 11 AM to 4 PM per comment)

        SELECT Username, ActualCount / Count
        FROM (
          SELECT U.Username, COUNT(1) AS Count, 
            SUM(CASE WHEN TIME(C.Date) BETWEEN '11:00:00' AND '16:00:00' THEN 1 ELSE 0 END) AS ActualCount
          FROM User U
            JOIN Comment C
              ON C.UserId = U.Id
          GROUP BY U.Username
          ) C
        WHERE Count > 0
        ORDER BY ActualCount / Count DESC LIMIT 3

* Nightowl (Comments from 2 AM to 4 AM)

        SELECT Username, Count
        FROM (
          SELECT U.Username, 
            SUM(CASE WHEN TIME(C.Date) BETWEEN '02:00:00' AND '04:00:00' THEN 1 ELSE 0 END) AS Count
          FROM User U
            JOIN Comment C
              ON C.UserId = U.Id
          GROUP BY U.Username
          ) C
        ORDER BY Count DESC LIMIT 3
    
* Nightowl / C (Comments from 2 AM to 4 AM per comment)

        SELECT Username, ActualCount / Count
        FROM (
          SELECT U.Username, COUNT(1) AS Count, 
            SUM(CASE WHEN TIME(C.Date) BETWEEN '02:00:00' AND '04:00:00' THEN 1 ELSE 0 END) AS ActualCount
          FROM User U
            JOIN Comment C
              ON C.UserId = U.Id
          GROUP BY U.Username
          ) C
        WHERE Count > 0
        ORDER BY ActualCount / Count DESC LIMIT 3

* Weekend (Comments on Sat/Sun)

        SELECT Username, Count
        FROM (
          SELECT U.Username,
            SUM(CASE WHEN WEEKDAY(C.Date) BETWEEN 5 AND 6 THEN 1 ELSE 0 END) AS Count
          FROM User U
            JOIN Comment C
              ON C.UserId = U.Id
          GROUP BY U.Username
          ) C
        WHERE Count > 0
        ORDER BY Count DESC LIMIT 3

* Weekend / C (Comments on Sat/Sun per comment)

        SELECT Username, ActualCount / Count
        FROM (
          SELECT U.Username, COUNT(1) AS Count, 
            SUM(CASE WHEN WEEKDAY(C.Date) BETWEEN 5 AND 6 THEN 1 ELSE 0 END) AS ActualCount
          FROM User U
            JOIN Comment C
              ON C.UserId = U.Id
          GROUP BY U.Username
          ) C
        WHERE Count > 0
        ORDER BY ActualCount / Count DESC LIMIT 3

* Self replies (Replies to self)
