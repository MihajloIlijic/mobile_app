import UIKit

var greeting = "{\"title\":\"Welcome MSD\"}"

print(greeting)
struct Message :Decodable{
    var title:String
}

enum WSError: Error{
    case illegalURL
 
    case decodeError
    case uexpectedStatus
}


func fetchMsg(nr:Int = 1) async throws-> Message{
    print("Fetching")
    
    var urlBuilder = URLComponents()
    urlBuilder.scheme = "http"
    urlBuilder.host = "localhost"
    urlBuilder.port = 9000
    urlBuilder.path = "message-\(nr).json"
    guard let curr_url = urlBuilder.url else{
        throw WSError.illegalURL
    }
    
    
    let request = URLRequest(url: curr_url)
    let (data, response) = try await URLSession.shared.data(for: request)
    
    guard (response as? HTTPURLResponse)?.statusCode == 200 else {
        throw WSError.uexpectedStatus
    }
    guard let fetchMsg = try? JSONDecoder().decode(Message.self, from: data)else{
        throw WSError.decodeError
    }
    
    return fetchMsg
}
Task{
    print("Background")
    let msg = try? await fetchMsg()
    if let m = msg {
        print("Successful downloaded: \(m)")
    }
    print("loading done")
    
}


if let data = greeting.data(using: .utf8){
    print(data)
    
    if let msgFromWS:Message = try? JSONDecoder().decode(Message.self, from: data){
        print("we got title: \(msgFromWS.title).")
    }
    else{
        print("null")
    }
}
    
