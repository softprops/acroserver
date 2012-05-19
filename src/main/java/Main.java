import org.jboss.netty.example.http.websocketx.server.Acronym;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class Main {

	public static void main(String...args) {
		Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).excludeFieldsWithoutExposeAnnotation().create();
		System.out.println(gson.toJson(new Acronym()));
	}
	
}
