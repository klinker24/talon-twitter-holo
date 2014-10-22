import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.*;
import org.apache.http.util.EntityUtils;
import sun.misc.BASE64Encoder;
import sun.rmi.runtime.Log;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.*;

public class GetLocations {
	public static void main(String[] args) {
		String data = "";
		try {
			//data = readFile("/home/luke/locations.txt");
            data = getJsonResponse();
			//System.out.print(data);
		} catch (Exception e) {
			System.out.print("error");
		}
		
		//printCountriesStringArray(data);
		//printLocationArrays(data);
		printReturnStatement(data);
	}
	
	public static void printCountriesStringArray(String data) {
		
		// get the list of countries
        ArrayList<String> countries = getCountries(data);
		
        // print out the formatted string array
		System.out.println("<string-array name=\"countries\">");
		
		for (String s: countries) {
			if (!s.equals("")) {
				System.out.println("<item>" + s + "</item>");
			}
		}
		
		System.out.println("</string-array>");
	}
	
	static class Country {
		public String name;
		public ArrayList<City> citys = new ArrayList<City>();
		
		public Country(String name) {
			this.name = name;
		}
	}
	
	static class City {
		public String name;
		public int woeid;
		
		public City(String name, int woeid) {
			this.name = name;
			this.woeid = woeid;
		}
	}
	
	public static void printLocationArrays(String data) {
		ArrayList<String> countries = getCountries(data);
		ArrayList<Country> array = new ArrayList<Country>();
		
		// set up the main array with the list of countries
		for (String s : countries) {
			array.add(new Country(s));
		}
		
		JSONArray jsonArray = new JSONArray(data);
		for (int i = 0; i < jsonArray.length(); i++) {
	        JSONObject jsonObject = jsonArray.getJSONObject(i);
	        String countName = jsonObject.getString("country");
	        
	        // add the city to the country
	        for (int j = 0; j < countries.size(); j++) {
	        	if (countName.equals(countries.get(j))) {
	        		String n = jsonObject.getString("name");
	        		if (n.equals(countName)) {
	        			n = "All Cities";
	        		}
	        		
	        		array.get(j).citys.add(new City(
	        				n, 
	        				jsonObject.getInt("woeid")));
	        	}
	        }
	    }
		
		for (Country c : array) {
			System.out.print("public static String[][] " + c.name.toLowerCase().replaceAll(" ", "_") + " = {\n");
			
			ArrayList<City> cities = c.citys;
			
			Collections.sort(cities, new Comparator<City>() {
	            public int compare(City result1, City result2) {
	                return result1.name.compareTo(result2.name);
	            }
	        });
			
			for (int i = 0; i < cities.size(); i++) {
				City s = cities.get(i);
				System.out.print("{\"" + s.name + "\", \"" + s.woeid + "\"}" + (i == cities.size() - 1 ? "\n" : ",\n"));
			}
			System.out.println("};\n");
		}
	}
	
	public static void printReturnStatement(String data) {
		ArrayList<String> countries = getCountries(data);
		
		for (String s : countries) {
			System.out.println("if (countryName.equals(\"" + s +"\")) {");
			System.out.print("return " + s.toLowerCase().replace(" ",  "_") + ";\n} else ");
		}
	}
	
	public static ArrayList<String> getCountries(String data) {
		ArrayList<String> countries = new ArrayList<String>();
		JSONArray jsonArray = new JSONArray(data);
		for (int i = 0; i < jsonArray.length(); i++) {
	        JSONObject jsonObject = jsonArray.getJSONObject(i);
	        countries.add(jsonObject.getString("country"));
	      }
		
		// add elements to al, including duplicates
		HashSet hs = new HashSet();
		hs.addAll(countries);
		countries.clear();
		countries.addAll(hs);
		
		Collections.sort(countries, new Comparator<String>() {
            public int compare(String result1, String result2) {
                return result1.compareTo(result2);
            }
        });
		return countries;
	}
	
	public static String readFile(String fileName) throws IOException {
	    BufferedReader br = new BufferedReader(new FileReader(fileName));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            line = br.readLine();
	        }
	        return sb.toString();
	    } finally {
	        br.close();
	    }
	}

    public static String getJsonResponse() {
        String json = "";
        try {
            String oauth_token = "604990177-TC8qkLBwjplYcfZy7E2GYGsDTx196BRjiq0r2Tl6";
            String oauth_token_secret = "ScT5prB4dgOlOVGEJA5lccyPM52iC5HkdBORBi6gjDzxc";

            // generate authorization header
            String get_or_post = "GET";
            String oauth_signature_method = "HMAC-SHA1";

            String uuid_string = UUID.randomUUID().toString();
            uuid_string = uuid_string.replaceAll("-", "");
            String oauth_nonce = uuid_string; // any relatively random alphanumeric string will work here

            // get the timestamp
            Calendar tempcal = Calendar.getInstance();
            long ts = tempcal.getTimeInMillis();// get current time in milliseconds
            String oauth_timestamp = (new Long(ts/1000)).toString(); // then divide by 1000 to get seconds

            // the parameter string must be in alphabetical order, "text" parameter added at end
            String parameter_string = "oauth_consumer_key=" + "V9yijGrKf79jlYi0l3ekpA" + "&oauth_nonce=" + oauth_nonce + "&oauth_signature_method=" + oauth_signature_method +
                    "&oauth_timestamp=" + oauth_timestamp + "&oauth_token=" + encode(oauth_token) + "&oauth_version=1.0";

            String twitter_endpoint = "https://api.twitter.com/1.1/trends/available.json";
            String twitter_endpoint_host = "api.twitter.com";
            String twitter_endpoint_path = "/1.1/trends/available.json";
            String signature_base_string = get_or_post + "&"+ encode(twitter_endpoint) + "&" + encode(parameter_string);
            String oauth_signature = computeSignature(signature_base_string, "IHHoYqukYC951gsP8gkhr1RUSBJYYwhGO0P3uuCDkA" + "&" + encode(oauth_token_secret));

            String authorization_header_string = "OAuth oauth_consumer_key=\"" + "V9yijGrKf79jlYi0l3ekpA" + "\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\"" + oauth_timestamp +
                    "\",oauth_nonce=\"" + oauth_nonce + "\",oauth_version=\"1.0\",oauth_signature=\"" + encode(oauth_signature) + "\",oauth_token=\"" + encode(oauth_token) + "\"";


            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, "UTF-8");
            HttpProtocolParams.setUserAgent(params, "HttpCore/1.1");
            HttpProtocolParams.setUseExpectContinue(params, false);
            HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpRequestInterceptor[] {
                    // Required protocol interceptors
                    new RequestContent(),
                    new RequestTargetHost(),
                    // Recommended protocol interceptors
                    new RequestConnControl(),
                    new RequestUserAgent(),
                    new RequestExpectContinue()});

            HttpRequestExecutor httpexecutor = new HttpRequestExecutor();
            HttpContext context = new BasicHttpContext(null);
            HttpHost host = new HttpHost(twitter_endpoint_host,443);
            DefaultHttpClientConnection conn = new DefaultHttpClientConnection();

            context.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
            context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, host);

            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, null, null);
            SSLSocketFactory ssf = sslcontext.getSocketFactory();
            Socket socket = ssf.createSocket();
            socket.connect(
                    new InetSocketAddress(host.getHostName(), host.getPort()), 0);
            conn.bind(socket, params);
            BasicHttpEntityEnclosingRequest request2 = new BasicHttpEntityEnclosingRequest("GET", twitter_endpoint_path);
            request2.setParams(params);
            request2.addHeader("Authorization", authorization_header_string);
            httpexecutor.preProcess(request2, httpproc, context);
            HttpResponse response2 = httpexecutor.execute(request2, conn, context);
            response2.setParams(params);
            httpexecutor.postProcess(response2, httpproc, context);
            json = EntityUtils.toString(response2.getEntity());
            conn.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public static String encode(String value)
    {
        String encoded = null;
        try {
            encoded = URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException ignore) {
        }
        StringBuilder buf = new StringBuilder(encoded.length());
        char focus;
        for (int i = 0; i < encoded.length(); i++) {
            focus = encoded.charAt(i);
            if (focus == '*') {
                buf.append("%2A");
            } else if (focus == '+') {
                buf.append("%20");
            } else if (focus == '%' && (i + 1) < encoded.length()
                    && encoded.charAt(i + 1) == '7' && encoded.charAt(i + 2) == 'E') {
                buf.append('~');
                i += 2;
            } else {
                buf.append(focus);
            }
        }
        return buf.toString();
    }

    private static String computeSignature(String baseString, String keyString) throws GeneralSecurityException, UnsupportedEncodingException
    {
        SecretKey secretKey = null;

        byte[] keyBytes = keyString.getBytes();
        secretKey = new SecretKeySpec(keyBytes, "HmacSHA1");

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(secretKey);

        byte[] text = baseString.getBytes();

        return new String(new BASE64Encoder().encode(mac.doFinal(text))).trim();
    }
}
