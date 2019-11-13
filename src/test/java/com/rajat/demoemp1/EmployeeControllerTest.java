package com.rajat.demoemp1;


import com.fasterxml.jackson.databind.ObjectMapper;

import com.jayway.jsonpath.JsonPath;

import com.rajat.ems.EmsApplication;
import com.rajat.ems.model.PostEmployeeRequestEntity;
import com.rajat.ems.model.PutEmployeeRequestEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.apache.commons.lang3.StringUtils;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@EnableWebMvc
@WebAppConfiguration
@ContextConfiguration(classes = {EmsApplication.class})
public class EmployeeControllerTest extends AbstractTransactionalTestNGSpringContextTests
{
    @Autowired
    WebApplicationContext context;
    private MockMvc mvc;
    @BeforeMethod
    public void setUp()
    {
       mvc= MockMvcBuilders.webAppContextSetup(context).build();
    }

    /***************************************** testcase for postApi ******************************************************/
       @Test(priority = 2)
    public void postNewEmployeeTest() throws Exception
    {
        PostEmployeeRequestEntity employeePost=new PostEmployeeRequestEntity("captain marvel","Intern",2);
        ObjectMapper mapper=new ObjectMapper();
        String jsonInput=mapper.writeValueAsString(employeePost);
        MvcResult result=mvc.perform(MockMvcRequestBuilders.post("/rest/employees").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
//        String resultOutput=result.getResponse().getContentAsString();
//        Assert.assertEquals("Employee Created",resultOutput)
    }

    @Test(priority = 1)
    public void postDirectorValidationForManager() throws Exception                //Assigning director under manager
    {
        PostEmployeeRequestEntity employeePost=new PostEmployeeRequestEntity("captain marvel","Director",2);
        ObjectMapper mapper=new ObjectMapper();
        String jsonInput=mapper.writeValueAsString(employeePost);
        mvc.perform(MockMvcRequestBuilders.post("/rest/employees").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
    }
    @Test
    public void postmultipleDirector() throws Exception                             //Adding another Director
    {
        PostEmployeeRequestEntity employeePost=new PostEmployeeRequestEntity("captain marvel","Director",null);
        ObjectMapper mapper=new ObjectMapper();
        String jsonInput=mapper.writeValueAsString(employeePost);
        mvc.perform(MockMvcRequestBuilders.post("/rest/employees").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
    }
    @Test
    public void postNoData () throws Exception                //Adding employee without any data
    {
        PostEmployeeRequestEntity employeePost=new PostEmployeeRequestEntity();
        ObjectMapper mapper=new ObjectMapper();
        String jsonInput=mapper.writeValueAsString(employeePost);
        mvc.perform(MockMvcRequestBuilders.post("/rest/employees").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
    }
    @Test
    public void postPartialData () throws Exception                //Adding employee with incomplete data
    {
        PostEmployeeRequestEntity employeePost=new PostEmployeeRequestEntity("captain marvel",2);
        ObjectMapper mapper=new ObjectMapper();
        String jsonInput=mapper.writeValueAsString(employeePost);
        mvc.perform(MockMvcRequestBuilders.post("/rest/employees").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
    }
    @Test
    public void postInvalidParentId () throws Exception                //Adding employee with invalid managerId
    {
        PostEmployeeRequestEntity employeePost=new PostEmployeeRequestEntity("captain marvel", "Lead",12);
        ObjectMapper mapper=new ObjectMapper();
        String jsonInput=mapper.writeValueAsString(employeePost);
        mvc.perform(MockMvcRequestBuilders.post("/rest/employees").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
    }
    @Test
    public void postHierarchyViolation () throws Exception                //Adding employee with violating hierarchy
    {
        PostEmployeeRequestEntity employee=new PostEmployeeRequestEntity("captain marvel", "Lead",8);
        ObjectMapper mapper=new ObjectMapper();
        String jsonInput=mapper.writeValueAsString(employee);
        mvc.perform(MockMvcRequestBuilders.post("/rest/employees").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();

        employee.setJobTitle("Manager");
        mvc.perform(MockMvcRequestBuilders.post("/rest/employees").content(mapper.writeValueAsString(employee)).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
    }
    @Test
    public void postInvalidDesignation () throws Exception                //Adding employee with non existing Designation
    {
        PostEmployeeRequestEntity employeePost=new PostEmployeeRequestEntity("captain marvel", "Laead",12);
        ObjectMapper mapper=new ObjectMapper();
        String jsonInput=mapper.writeValueAsString(employeePost);
        mvc.perform(MockMvcRequestBuilders.post("/rest/employees").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
    }
    /*********************** testcase for delete employee ********************/


    @Test
    public void delInvalidId () throws Exception                //Deleting non existing employee
    {
        mvc.perform(MockMvcRequestBuilders.delete("/rest/employees/121"))
                .andExpect(MockMvcResultMatchers.status().isNotFound()).andReturn();
    }
    @Test
    public void delDirectorWithChild () throws Exception                //Deleting director with children
    {
        mvc.perform(MockMvcRequestBuilders.delete("/rest/employees/1"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
    }
    @Test
    public void delDirectorWithoutChild () throws Exception                //Deleting director with children
    {
        mvc.perform(MockMvcRequestBuilders.delete("/rest/employees/1"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
    }
    @Test
    public void delAllEmployees() throws Exception                //Deleting director without children
    {
        for(int empId=10;empId>0;empId--)
        {
            mvc.perform(MockMvcRequestBuilders.delete("/rest/employees/"+empId))
                    .andExpect(MockMvcResultMatchers.status().isNoContent()).andReturn();
        }
    }


    /*********************** testcase for put false  ********************/
    @Test
    public void updateEmpInvalidId() throws Exception                               // updating an nonExisting employee
    {
        PutEmployeeRequestEntity employeePut = new PutEmployeeRequestEntity("Rajat","Manager",2,false);
        ObjectMapper mapper = new ObjectMapper();
        String jsonInput = mapper.writeValueAsString(employeePut);
        mvc.perform(MockMvcRequestBuilders.put("/rest/employees/13").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
    }

    @Test
    public void updateEmpNoData() throws Exception                                                           //updating employee without any data
    {
        PutEmployeeRequestEntity employeePut = new PutEmployeeRequestEntity("","",null,false);
        ObjectMapper mapper = new ObjectMapper();
        String jsonInput = mapper.writeValueAsString(employeePut);
        mvc.perform(MockMvcRequestBuilders.put("/rest/employees/2").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();

    }

    @Test
    public void updateEmpInvalidParId() throws Exception                                                 //updating employee with invalid managerId
    {
        PutEmployeeRequestEntity employeePut = new PutEmployeeRequestEntity("Mohit","Manager",13,false);
        ObjectMapper mapper = new ObjectMapper();
        String jsonInput = mapper.writeValueAsString(employeePut);
        mvc.perform(MockMvcRequestBuilders.put("/rest/employees/2").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();

    }

    @Test
    public void updateEmpPromotion() throws Exception                                                            // promoting manager to director
    {
        PutEmployeeRequestEntity employeePut = new PutEmployeeRequestEntity("Mohit","Director",1,false);
        ObjectMapper mapper = new ObjectMapper();
        String jsonInput = mapper.writeValueAsString(employeePut);
        mvc.perform(MockMvcRequestBuilders.put("/rest/employees/2").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();

    }

    @Test
    public void updateEmpDemotion() throws Exception                                                                //demoting manager to lead
    {
        PutEmployeeRequestEntity employeePut = new PutEmployeeRequestEntity("Mohit","lead",1,false);
        ObjectMapper mapper = new ObjectMapper();
        String jsonInput = mapper.writeValueAsString(employeePut);
        mvc.perform(MockMvcRequestBuilders.put("/rest/employees/4").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();

    }

//    @Test
//    public void updateEmpDemotionIron() throws Exception
//    {
//        PutEmployeeRequestEntity employeePut = new PutEmployeeRequestEntity("LEAD",false);
//
//        ObjectMapper mapper = new ObjectMapper();
//        String jsonInput = mapper.writeValueAsString(employeePut);
//        mvc.perform(MockMvcRequestBuilders.put("/rest/employees/2").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
//                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
////        String resultOutput=result.getResponse().getContentAsString();
////        System.out.println(resultOutput);
    //  }

    @Test
    public void updateEmpDemoteDirector() throws Exception                                                 // demoting director
    {
        PutEmployeeRequestEntity employeePut = new PutEmployeeRequestEntity("","lead",null,false);
        ObjectMapper mapper = new ObjectMapper();
        String jsonInput = mapper.writeValueAsString(employeePut);
        mvc.perform(MockMvcRequestBuilders.put("/rest/employees/1").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();

    }

    @Test
    public void updateEmpDirectorName() throws Exception                                                         // updating director name
    {
        PutEmployeeRequestEntity employeePut = new PutEmployeeRequestEntity("Rajat","",null,false);
        ObjectMapper mapper = new ObjectMapper();
        String jsonInput = mapper.writeValueAsString(employeePut);
        mvc.perform(MockMvcRequestBuilders.put("/rest/employees/1").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

    }

    @Test
    public void updateEmpDirectorWithDirector() throws Exception                                                 // updating director designation to director
    {
        PutEmployeeRequestEntity employeePut = new PutEmployeeRequestEntity("","Director",null,false);
        ObjectMapper mapper = new ObjectMapper();
        String jsonInput = mapper.writeValueAsString(employeePut);
        mvc.perform(MockMvcRequestBuilders.put("/rest/employees/1").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

    }

    //    @Test
//    public void updateEmpDirectorWithOutDirector() throws Exception                                               //
//    {
//        PutEmployeeRequestEntity employeePut = new PutEmployeeRequestEntity("","manager",null,false);
//        ObjectMapper mapper = new ObjectMapper();
//        String jsonInput = mapper.writeValueAsString(employeePut);
//        mvc.perform(MockMvcRequestBuilders.put("/rest/employees/1").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
//                .andExpect(MockMvcResultMatchers.status().isForbidden()).andReturn();
//
//    }
    @Test
    public void updateEmpDirectorParChange() throws Exception                                           //updating managerId of director
    {
        PutEmployeeRequestEntity employeePut = new PutEmployeeRequestEntity("","",2,false);
        ObjectMapper mapper = new ObjectMapper();
        String jsonInput = mapper.writeValueAsString(employeePut);
        mvc.perform(MockMvcRequestBuilders.put("/rest/employees/1").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();

    }

    @Test
    public void updateHulkChildfOfCaptain() throws Exception                                             // making hulk subordinate of captainAmerica
    {
        PutEmployeeRequestEntity employeePut = new PutEmployeeRequestEntity("","",4,false);
        ObjectMapper mapper = new ObjectMapper();
        String jsonInput = mapper.writeValueAsString(employeePut);
        mvc.perform(MockMvcRequestBuilders.put("/rest/employees/3").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

    }

    /************************ testcase for put true *****************************/
    @Test
    public void putEmpWithNoData() throws Exception
    {
        PutEmployeeRequestEntity employeePut = new PutEmployeeRequestEntity("","",null,true);
        ObjectMapper mapper = new ObjectMapper();
        String jsonInput = mapper.writeValueAsString(employeePut);
        mvc.perform(MockMvcRequestBuilders.put("/rest/employees/3").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
    }

    @Test
    public void putEmpWithInvalidPartialData() throws Exception
    {
        PutEmployeeRequestEntity employeePut = new PutEmployeeRequestEntity("Captain Marvel","",null,true);
        ObjectMapper mapper = new ObjectMapper();
        String jsonInput = mapper.writeValueAsString(employeePut);
        mvc.perform(MockMvcRequestBuilders.put("/rest/employees/3").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
    }

    @Test
    public void putEmpWithDemotion() throws Exception
    {
        PutEmployeeRequestEntity employeePut = new PutEmployeeRequestEntity("Captain Marvel","intern",null,true);
        ObjectMapper mapper = new ObjectMapper();
        String jsonInput = mapper.writeValueAsString(employeePut);
        mvc.perform(MockMvcRequestBuilders.put("/rest/employees/3").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
    }

    @Test
    public void putEmpWithDemotionPossible() throws Exception
    {
        PutEmployeeRequestEntity employeePut = new PutEmployeeRequestEntity("Captain Marvel","Lead",null,true);
        ObjectMapper mapper = new ObjectMapper();
        String jsonInput = mapper.writeValueAsString(employeePut);
        mvc.perform(MockMvcRequestBuilders.put("/rest/employees/2").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    @Test
    public void putEmpWithPromotionPossible() throws Exception
    {
        PutEmployeeRequestEntity employeePut = new PutEmployeeRequestEntity("Captain Marvel","Manager",null,true);
        ObjectMapper mapper = new ObjectMapper();
        String jsonInput = mapper.writeValueAsString(employeePut);
        mvc.perform(MockMvcRequestBuilders.put("/rest/employees/3").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    @Test
    public void putEmpWithPromotion() throws Exception
    {
        PutEmployeeRequestEntity employeePut = new PutEmployeeRequestEntity("Captain Marvel","Director",null,true);
        ObjectMapper mapper = new ObjectMapper();
        String jsonInput = mapper.writeValueAsString(employeePut);
        mvc.perform(MockMvcRequestBuilders.put("/rest/employees/2").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
    }

    @Test
    public void replaceWithDirector() throws Exception
    {
        PutEmployeeRequestEntity employeePut = new PutEmployeeRequestEntity("Captain Marvel","Director",null,true);
        ObjectMapper mapper = new ObjectMapper();
        String jsonInput = mapper.writeValueAsString(employeePut);
        mvc.perform(MockMvcRequestBuilders.put("/rest/employees/1").content(jsonInput).contentType(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    /*************************************** testcase for get ****************************************/

    @Test(priority = 1)
    public void getAllTest() throws Exception
    {
        MvcResult result=mvc.perform(MockMvcRequestBuilders.get("/rest/employees"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).andReturn();
        String jsonOutput=result.getResponse().getContentAsString();
        int length= JsonPath.parse(jsonOutput).read("$.length()");
        Assert.assertTrue(length>0);
    }
    //Test for get Specific
    @Test(priority = 2)
    public void getUser() throws Exception
    {
        MvcResult result=mvc.perform(MockMvcRequestBuilders.get("/rest/employees/1"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).andDo(print()).andReturn();
        String jsonOutput=result.getResponse().getContentAsString();
        int length= JsonPath.parse(jsonOutput).read("$.length()");
        System.out.println(length);
        Assert.assertTrue(length>0);
    }
    @Test(priority = 3)
    public void getUserInvalidId() throws Exception
    {
        MvcResult result=mvc.perform(MockMvcRequestBuilders.get("/rest/employees/11"))
                .andExpect(MockMvcResultMatchers.status().isNotFound()).andDo(print()).andReturn();
    }
    @Test(priority = 4)
    public void getUserNullId() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/rest/employees/null"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andDo(print()).andReturn();
    }


}
