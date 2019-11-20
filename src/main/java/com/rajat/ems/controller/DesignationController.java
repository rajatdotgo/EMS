package com.rajat.ems.controller;


import com.rajat.ems.model.PostDesignationRequestBody;
import com.rajat.ems.repository.DesignationRepo;
import com.rajat.ems.service.DesignationService;
import com.rajat.ems.util.MessageConstant;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class DesignationController {
    private DesignationRepo designationRepo;
    private final MessageConstant message;
    private final DesignationService designationService;

    @Autowired
    DesignationController(DesignationRepo designationRepo,MessageConstant message,DesignationService designationService){
        this.designationRepo=designationRepo;
        this.message=message;
        this.designationService=designationService;
    }

    @GetMapping(path = "/rest/designations")
    @ApiOperation(value = "Finds all the designation in sorted order ")
    public ResponseEntity allDesignations() {
        return new ResponseEntity<>(designationService.getAllDesignation(), HttpStatus.OK);
    }

    @DeleteMapping(path = "/rest/designations/{designationName}")
    @ApiOperation(value = "Delete an designation by name otherwise suitable response")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully deleted designation"),
            @ApiResponse(code = 404, message = "Designation not found"),
    })
    public ResponseEntity deleteDesignation(@ApiParam(value = "Designation name which you want to delete", example = "Manager", required = true) @PathVariable("designationName") String designationName) {

        designationService.deleteDesignation(designationName);
        return new ResponseEntity<>(message.getMessage("DELETED"),HttpStatus.NO_CONTENT);
    }

    @PostMapping(path = "/rest/designations")
    @ApiOperation(value = "Adds a new employee in the organisation")
    public ResponseEntity saveData(@RequestBody PostDesignationRequestBody designation) {

        designationService.addDesignation(designation);
        return new ResponseEntity<>(message.getMessage("DESIGNATION_ADDED"),HttpStatus.CREATED);
    }

}
