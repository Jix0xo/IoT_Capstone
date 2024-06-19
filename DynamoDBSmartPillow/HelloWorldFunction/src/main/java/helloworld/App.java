package helloworld;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

// App 클래스는 AWS Lambda의 RequestHandler 인터페이스를 구현하여 DynamoDB에 데이터를 저장합니다.
public class App implements RequestHandler<Document, String> {

    // DynamoDB 클라이언트를 위한 DynamoDB 객체.
    private DynamoDB dynamoDb;
    // DynamoDB 테이블 이름.
    private String DYNAMODB_TABLE_NAME = "SmartPillowLog";

    // Lambda 함수의 진입점인 handleRequest 메서드.
    @Override
    public String handleRequest(Document input, Context context) {
        // DynamoDB 클라이언트 초기화.
        this.initDynamoDbClient();
        context.getLogger().log("Input: " + input);

        // 데이터를 DynamoDB에 저장하고 결과를 반환.
        return persistData(input);
    }

    // 데이터를 DynamoDB에 저장하는 메서드.
    private String persistData(Document document) throws ConditionalCheckFailedException {
        // Unix 타임스탬프를 형식화된 문자열로 변환.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String timeString = sdf.format(new java.util.Date(document.timestamp * 1000));

        // 현재 상태가 이전 상태와 동일한 경우 저장하지 않고 종료.
        if (document.current.state.reported.posture.equals(document.previous.state.reported.posture) &&
                document.current.state.reported.startTime.equals(document.previous.state.reported.startTime) &&
                document.current.state.reported.endTime.equals(document.previous.state.reported.endTime) &&
                document.current.state.reported.moving.equals(document.previous.state.reported.moving) &&
                document.current.state.reported.snoring.equals(document.previous.state.reported.snoring)) {
            return null;
        }

        // 데이터를 DynamoDB 테이블에 저장.
        return this.dynamoDb.getTable(DYNAMODB_TABLE_NAME)
                .putItem(new PutItemSpec().withItem(new Item()
                        .withPrimaryKey("deviceId", document.device)
                        .withLong("time", document.timestamp)
                        .withString("posture", document.current.state.reported.posture)
                        .withString("startTime", document.current.state.reported.startTime)
                        .withString("endTime", document.current.state.reported.endTime)
                        .withString("moving", document.current.state.reported.moving)
                        .withString("snoring", document.current.state.reported.snoring)
                        .withString("timestamp", timeString)))
                .toString();
    }

    // DynamoDB 클라이언트를 초기화하는 메서드.
    private void initDynamoDbClient() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion("ap-northeast-2").build();
        this.dynamoDb = new DynamoDB(client);
    }
}

// Document 클래스는 Lambda 함수에 전달되는 데이터를 나타냅니다.
class Document {
    public Thing previous; // 이전 상태.
    public Thing current;  // 현재 상태.
    public long timestamp; // 타임스탬프.
    public String device;  // AWS IoT에 등록된 사물 이름.
}

// Thing 클래스는 사물의 상태를 나타냅니다.
class Thing {
    public State state = new State(); // 상태 정보.
    public long timestamp; // 타임스탬프.
    public String clientToken; // 클라이언트 토큰.

    // State 클래스는 상태 정보를 포함합니다.
    public class State {
        public Tag reported = new Tag(); // 보고된 상태.
        public Tag desired = new Tag();  // 원하는 상태.

        // Tag 클래스는 상태의 세부 정보를 포함합니다.
        public class Tag {
            public String posture;  // 자세.
            public String startTime; // 시작 시간.
            public String endTime;   // 종료 시간.
            public String moving;    // 움직임.
            public String snoring;   // 코골이.
        }
    }
}
