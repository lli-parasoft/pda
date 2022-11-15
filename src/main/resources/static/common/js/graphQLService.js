angular
    .module('pdaApp')
        .factory('graphQLService', function($http, $filter) {
            let instance = {};
            let formatError = (response) => {
                return {
                    data: {
                        data: response.data.errors[0].extensions.data,
                        message: response.data.errors[0].message
                    },
                    status: response.data.errors[0].extensions.statusCode,
                    config: response.config
                };
            };

            let makeCall = function(requestBody, success, error) {
                $http({
                    method: 'POST',
                    url: '/proxy/graphql',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    data: requestBody
                }).then(function(response) {
                    if (response.data.errors) {
                        error(formatError(response));
                    } else {
                        success(response);
                    }
                }).catch(function(response) {
                    error(response)
                });
            };
            // categories
            instance.getCategories = function(success, error) {
                let requestBody = {"query": "{getCategories{totalElements,totalPages,size,number,numberOfElements,sort,content{id,name,description,image}}}"}
                makeCall(requestBody, function(response) {
                    success(response.data.data.getCategories);
                }, error);
            }

            instance.getCategoryById = function(variables, success, error) {
                let requestBody = {"query": "query($categoryId:ID){getCategoryById(categoryId:$categoryId){id,name,description,image}}", "variables": variables}
                makeCall(requestBody, function(response) {
                    success(response.data.data.getCategoryById);
                }, error);
            }

            // locations
            instance.getLocation = function(variables, success, error) {
                let requestBody = {"query": "query($region:RegionType!){getLocation(region:$region){id,locationInfo,locationImage}}", "variables": variables}
                makeCall(requestBody, function(response) {
                    success(response.data.data.getLocation);
                }, error);
            }
            // Orders
            instance.createOrder = function(variables, success, error) {
                let requestBody = {
                    "query": "mutation($orderDTO:OrderDTO!){" +
                            "createOrder(orderDTO: $orderDTO)" +
                                "{" +
                                    "id,orderNumber,requestedBy,status,reviewedByAPV,reviewedByPRCH,respondedBy," +
                                    "orderItems" +
                                        "{" +
                                            "id,name,description,image,itemId,quantity" +
                                        "}," +
                                    "region,location,orderImage,receiverId,eventId,eventNumber,submissionDate,approverReplyDate,comments}" +
                                "}",
                                "variables": variables}
                makeCall(requestBody, function(response) {
                    success(response.data.data.createOrder);
                }, error);
            }
            // items
            instance.getItems = function (variables, success, error) {
                let requestBody = {"query": "query($categoryId: Int, $regions: [RegionType], $searchString: String, $page: Int, $size: Int, $sort: [String])" +
                        "{getItems(categoryId: $categoryId, regions: $regions, searchString: $searchString, page: $page, size: $size, sort: $sort){" +
                        "totalElements,totalPages,size,number,numberOfElements,sort,content{id,name,description,inStock,image,region,lastAccessedDate,categoryId}}}", "variables": variables};
                makeCall(requestBody, function (response) {
                    success(response.data.data.getItems);
                }, error);
            }
            return instance;
        });