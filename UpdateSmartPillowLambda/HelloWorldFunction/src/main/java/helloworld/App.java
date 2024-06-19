package helloworld;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.iotdata.AWSIotData;
import com.amazonaws.services.iotdata.AWSIotDataClientBuilder;
import com.amazonaws.services.iotdata.model.UpdateThingShadowRequest;
import com.amazonaws.services.iotdata.model.UpdateThingShadowResult;
import com.fasterxml.jackson.annotation.JsonCreator;

// App 클래스는 AWS Lambda의 RequestHandler 인터페이스를 구현하여 이벤트를 처리합니다.
public class App implements RequestHandler<Event, String> {

    // Lambda 함수의 진입점인 handleRequest 메서드.
    public String handleRequest(final Event event, final Context context) {
        // AWS IoT Data 클라이언트를 생성하여 AWS IoT와 상호작용합니다.
        AWSIotData iotData = AWSIotDataClientBuilder.standard().build();

        // 이벤트의 태그를 JSON 형식의 페이로드 문자열로 변환합니다.
        String payload = getPayload(event.tags);

        // UpdateThingShadowRequest 객체를 생성하여 Thing의 섀도우를 업데이트합니다.
        UpdateThingShadowRequest updateThingShadowRequest  =
                new UpdateThingShadowRequest()
                        .withThingName(event.device) // Thing 이름을 설정합니다.
                        .withPayload(ByteBuffer.wrap(payload.getBytes())); // 페이로드를 바이트 버퍼로 설정합니다.

        // Thing 섀도우를 업데이트하고 결과를 가져옵니다.
        UpdateThingShadowResult result = iotData.updateThingShadow(updateThingShadowRequest);

        // 결과 페이로드를 바이트 배열로 읽어 문자열로 변환합니다.
        byte[] bytes = new byte[result.getPayload().remaining()];
        result.getPayload().get(bytes);
        String output = new String(bytes);

        // 변환된 문자열을 반환합니다.
        return output;
    }

    // 태그 목록을 JSON 형식의 페이로드 문자열로 변환하는 getPayload 메서드.
    private String getPayload(ArrayList<Tag> tags) {
        String tagstr = "";
        for (int i = 0; i < tags.size(); i++) {
            if (i != 0) tagstr += ", "; // 각 태그 사이에 쉼표를 추가합니다.
            tagstr += String.format("\"%s\" : \"%s\"", tags.get(i).tagName, tags.get(i).tagValue); // 태그를 JSON 키-값 쌍으로 형식화합니다.
        }
        // JSON 형식의 상태 문자열을 반환합니다.
        return String.format("{ \"state\": { \"desired\": { %s } } }", tagstr);
    }
}

// Event 클래스는 Lambda 함수에 전달되는 이벤트를 나타냅니다.
class Event {
    public String device; // IoT 디바이스(Thing)의 이름.
    public ArrayList<Tag> tags; // 태그 목록.

    // 기본 생성자는 태그 목록을 초기화합니다.
    public Event() {
        tags = new ArrayList<Tag>();
    }
}

// Tag 클래스는 키-값 쌍을 나타냅니다.
class Tag {
    public String tagName; // 태그 이름.
    public String tagValue; // 태그 값.

    // JSON 역직렬화를 위한 기본 생성자.
    @JsonCreator
    public Tag() {
    }

    // 매개변수가 있는 생성자는 태그 이름과 값을 초기화합니다.
    public Tag(String n, String v) {
        tagName = n;
        tagValue = v;
    }
}
