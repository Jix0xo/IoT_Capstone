package helloworld;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.TimeZone;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

// App 클래스는 AWS Lambda의 RequestHandler 인터페이스를 구현하여 DynamoDB에서 데이터를 쿼리합니다.
public class App implements RequestHandler<Event, String> {
    // DynamoDB 클라이언트를 위한 DynamoDB 객체.
    private DynamoDB dynamoDb;
    // DynamoDB 테이블 이름.
    private String DYNAMODB_TABLE_NAME = "SmartPillowLog";

    // Lambda 함수의 진입점인 handleRequest 메서드.
    public String handleRequest(final Event input, final Context context) {
        // DynamoDB 클라이언트 초기화.
        this.initDynamoDbClient();
        Table table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);

        long from = 0;
        long to = 0;
        try {
            // 날짜와 시간을 파싱하기 위한 SimpleDateFormat 객체 생성.
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

            // 입력된 날짜 문자열을 Unix 타임스탬프로 변환.
            from = sdf.parse(input.from).getTime() / 1000;
            to = sdf.parse(input.to).getTime() / 1000;
        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        // QuerySpec 객체를 생성하여 DynamoDB 쿼리 조건 설정.
        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("deviceId = :v_id and #t between :from and :to")
                .withNameMap(new NameMap().with("#t", "time"))
                .withValueMap(new ValueMap().withString(":v_id", input.device).withNumber(":from", from).withNumber(":to", to));

        ItemCollection<QueryOutcome> items = null;
        try {
            // 쿼리 실행.
            items = table.query(querySpec);
        } catch (Exception e) {
            System.err.println("Unable to scan the table:");
            System.err.println(e.getMessage());
        }

        // 쿼리 결과를 JSON 형식의 문자열로 변환.
        String output = getResponse(items);
        return output;
    }

    // 쿼리 결과를 JSON 형식의 문자열로 변환하는 메서드.
    private String getResponse(ItemCollection<QueryOutcome> items) {
        Iterator<Item> iter = items.iterator();
        String response = "{ \"data\": [";
        for (int i = 0; iter.hasNext(); i++) {
            if (i != 0)
                response += ",";
            response += iter.next().toJSON();
        }
        response += "]}";
        return response;
    }

    // DynamoDB 클라이언트를 초기화하는 메서드.
    private void initDynamoDbClient() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        this.dynamoDb = new DynamoDB(client);
    }
}

// Event 클래스는 Lambda 함수에 전달되는 이벤트를 나타냅니다.
class Event {
    public String device; // 디바이스 ID.
    public String from;   // 시작 날짜와 시간.
    public String to;     // 종료 날짜와 시간.
}
