package eci.cosw.controller;


import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import eci.cosw.AppConfiguration;
import eci.cosw.data.TodoRepository;
import eci.cosw.data.model.Todo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@RequestMapping("api")
@RestController
public class RESTController {


    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    TodoRepository todoRepository;

    @RequestMapping(value = "/files/{filename}", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> getFileByName(@PathVariable String filename) throws IOException {


        GridFSFile file = gridFsTemplate.findOne(new Query().addCriteria(Criteria.where("filename").is(filename)));
        if(file == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else{
            GridFsResource resource = gridFsTemplate.getResource(file.getFilename());
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(file.getMetadata().get("contentType").toString()))
                    .body(new InputStreamResource(resource.getInputStream()));
        }

    }

    @CrossOrigin("*")
    @PostMapping("/files")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) throws IOException {
        DBObject metaData = new BasicDBObject();
        metaData.put("contentType", file.getContentType());
        URL url = new URL("https://images.clarin.com/2017/12/11/SyDPzEh-G_1200x0.jpg");
        gridFsTemplate.store(url.openStream(), file.getName(), metaData);
        return null;
    }

    @CrossOrigin("*")
    @PostMapping("/todo")
    public Todo createTodo(@RequestBody Todo todo) {
        todoRepository.save(todo);
        return todo;
    }

    @CrossOrigin("*")
    @GetMapping("/todo")
    public List<Todo> getTodoList() {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfiguration.class);
        MongoOperations mongoOperation = (MongoOperations) applicationContext.getBean("mongoTemplate");
        Query firstQuery = new Query();
        List<Todo> todos1 = mongoOperation.find(firstQuery, Todo.class);
        for(Todo todo: todos1){
            System.out.println("Due Date: " + todo.getDueDate() + "           Description: " + todo.getDescription());
        }
        return todos1;
    }

}
