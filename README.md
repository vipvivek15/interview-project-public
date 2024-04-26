# Shepherd Money Interview Project

## How I tested

First I ensured the project builds with gradle. To ensure the project build the sdk needs to be be openjdk17-jdk oracle, the language level needs to be 17. After this, I ensured that gradle is using jdk 17 by setting the environment variable for JAVA_HOME as jdk-17. Since I have a windows computer, I had to install jdk-17 since I did not have it installed earlier. 
Link to install java jdk-17 for windows: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html

After ensuring gradle uses java jdk-17, I first ran ./gradlew build to build the project and generate a jar file which I have attached in the main Github directory. 
Then I ran ./gradlew bootrun to run the java application which starts the tomcat server. 
While making queries, I also accessed the database located at this URL: http://localhost:8080/h2-ui

I made the queries using Invoke-WebRequest. Here are some sample queries I made to test all possible edge cases:

1) Creation of a user
$response = Invoke-WebRequest -Method Put `
>>                   -Uri "http://localhost:8080/user" `
>>                   -ContentType "application/json" `
>>                   -Body '{
>>                           "name": "Vivek Ponnala",
>>                           "email": "vipvivek15@gmail.com"
>>                         }'
2) Adding credit card to a user
 $response = Invoke-WebRequest -Method Post `
>>                               -Uri "http://localhost:8080/credit-card" `
>>                               -ContentType "application/json" `
>>                               -Body '{
>>                                       "userId": 11202,
>>                                       "cardNumber": "987",
>>                                       "cardIssuanceBank": "Chase"
>>                                     }'
3) Getting all credit card information of a user
    $response = Invoke-WebRequest -Method Get `
>>                               -Uri "http://localhost:8080/credit-card/all/11202" `
>>                               -Headers @{Accept="application/json"}
4) Getting the user id from the credit card number
   $response = Invoke-WebRequest -Method Get `
>>                               -Uri "http://localhost:8080/credit-card/user-id/4111111111111111" `
>>                               -Headers @{Accept="application/json"}
5) Updating the balance of a credit card
   $response = Invoke-WebRequest -Method Post `
>>                               -Uri "http://localhost:8080/credit-card/update-balance" `
>>                               -ContentType "application/json" `
>>                               -Body '[
>>                                       {
>>                                         "creditCardNumber": "493",
>>                                        "balanceDate": "2024-04-11",
>>                                         "balanceAmount": 470
>>                                       },
>>                                       {
>>                                         "creditCardNumber": "493",
>>                                         "balanceDate": "2024-04-14",
>>                                         "balanceAmount": 430
>>                                       }
>>                                      ]'
6) Deleting a user with the userId
$response = Invoke-WebRequest -Method Delete `
>>                               -Uri "http://localhost:8080/user/11202" `
>>                               -UseBasicParsing

## Changes made in code

 @GetMapping("/credit-card:all")                                                                   ->           @GetMapping("/credit-card/all/{userId}")
                    

@GetMapping("/credit-card:user-id")                                                                ->            @GetMapping("/credit-card/user-id/{creditCardNumber}")


@PostMapping("/credit-card:update-balance")                                                        ->            @PostMapping("/credit-card/update-balance")


@DeleteMapping("/user")                                                                            ->            @DeleteMapping("/user/{userId}")    

## Use of Generative AI tools

I have used generative AI tools in the process of completing this project to assist with implementation details. 

## Introduction

Thanks for your interest in applying to Shepherd Money! Complete this short toy project before your interview to help us evaluate your skills as a software engineer. It shouldn't take more than an hour if you know Spring Boot. We look forward to seeing your work and learning more about you!

## Submission
Create a public repository on Github or Gitlab with the code committed to the `main` branch. Send the repository link to bofanxu@shepherdmoney.com. Make sure that your code is committed to the top level git root.

## Testing and What We Are Looking For
Feel free to test your solution with your own inputs as we don't provide local test cases. To run the project, use the code below. JDK 17 is required, so make sure it's installed on your computer. If you're using Debian Linux, you can install it with `sudo apt install openjdk-17-jdk`.

```bash
./gradlew bootRun
```

We will test your solution by calling the controller APIs directly. While correctness is important, we are more interested in how you designed the API and your coding practices, such as using clear variable names, good comments, and good naming conventions.

## Project Summary
Write a Spring Boot program that manages user creation/deletion and adding credit cards to their profiles. Users may have zero or more credit cards associated with them. Also, create two APIs: one to get all credit cards for a user and another to find a user by their credit card number. There is an additional API that update the balance history of a credit card.

## Files to Change
Any files with content marked with **TODO** will contains hints on what to add. You are welcomed to add/modify any files to help to implement this project.

## Models
Following is a component overview of the models. You can find more hints in the source code files as well
- `User`: each user has their name, date of birth, and some (or none) credit cards associated with them
- `CreditCard`: each credit card has issance bank, card number, who the card belongs to, and a list of balance history
- `BalanceHistory`: credit card balance for a specific date.

## Controllers
The controllers should contain enough comment for you to implement their basic functionalities. Note that apart from implementing the simple functionalities, we are looking for good coding conventions, good error handling, etc.

## Useful Tools
- **PostMan**: useful to send http requests to test your API
- **H2 Console**: when running your project, you can use `http://localhost:8080/h2-ui` to access the h2 console. This will allow to look at what's stored in the database.
  - The database name is `database`
  - The username is `sa`
  - The password is `password`

## Use of Generative AI Tools
We **welcome** you to use generative AI tools to complete this project :). We believe generative AI tools can be powerful assistants to SDEs, if used appropriately. However, you must **disclose** its usage when you submit your code. Just append a short sentence in your email submission saying that

```
I have used generative AI tools in the process of completing this project.
```

Your chances of getting an interview and/or being given an offer is not affected by this. **However**, as frequent users of generative AI tools, we are keen in spotting traces of generated code. If we believe you have used AI tools without disclosing, we will not move forward with your application.

## Questions?
If you have project related issues, feel free to raise an issue on the repository :D. If you have more private questions, please reach out to bofanxu@shepherdmoney.com
