package iss.nus.MongoAggregator.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iss.nus.MongoAggregator.repositories.GamesRepository;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;

@RestController
@RequestMapping(path="/games")
public class GamesRestController {

    @Autowired
    GamesRepository repo;

    @GetMapping(path="/simple/{game_id}", produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAggregation(@PathVariable(name="game_id") String id) {

            Integer gid = null;
            
            try {
                gid = Integer.parseInt(id);

            } catch (Exception e) {

                return ResponseEntity.badRequest().body("'error': 'oi, dont break my app'");
            }

            Optional<String> opt = repo.getSimpleAggregation(gid);

            if(opt.isEmpty()) {
                return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Json.createObjectBuilder()
                        .add("status", HttpStatus.NOT_FOUND.value())
                        .add("timestamp", LocalDateTime.now().toString())
                        .add("error", "No results found. Please check your database / collection names")
                        .build().toString());
            }

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(opt.get());

    }

    @GetMapping(path="/complex/{search_term}")
    public ResponseEntity<String> getComplexQuery(@PathVariable(name="search_term") String searchTerm) {

        List<Document> results = repo.getComplexAggregation(searchTerm);

        if(results.isEmpty()) {
            return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Json.createObjectBuilder()
                    .add("status", HttpStatus.OK.value())
                    .add("timestamp", LocalDateTime.now().toString())
                    .add("message", "No results found")
                    .build().toString());
        }

        return ResponseEntity
        .status(HttpStatus.OK)
        .contentType(MediaType.APPLICATION_JSON)
        .body(results.get(0).toJson());
    }

}
