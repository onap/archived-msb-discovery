/*
 * Copyright 2016-2017 ZTE, Inc. and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
$(function(){


   $(".form-tip").blur(function(){
    if($.trim($(this).val())==""){
      $(this).removeClass("form-input-focus");
      $(this).prev().removeClass("item-tip-focus");
    }});

$(".form-tip").focus(function(){
  if(!$(this).hasClass("form-input-focus")){
  $(this).addClass("form-input-focus");
  $(this).prev().addClass("item-tip-focus");
  }});

$(".item-tip").click(function(){
  $(this).next().focus();
});

$("input[name='version']").blur(function(){
    $(this).val($(this).val().toLowerCase());

    if(vm.msbRouteInfo.protocol=='REST' || vm.msbRouteInfo.protocol=='HTTP' ){
    routeUtil.changeTargetServiceUrl();
    }
});



$("input[name='serviceName']").change(function(){
 if(vm.msbRouteInfo.protocol=='REST' || vm.msbRouteInfo.protocol=='UI' || vm.msbRouteInfo.protocol=='HTTP'){
    routeUtil.changeTargetServiceUrl();
    }

});

$("input[name='url']").change(function(){

    routeUtil.changeTargetServiceUrl();
});



$("select[name='protocol']").change(function(){

routeUtil.changeTargetServiceUrl();

});

$("input[name='chkVisualRange']").change(function(){

routeUtil.changeTargetServiceUrl();

});

$("input[name='publish_port']").change(function(){

routeUtil.changeTargetServiceUrl();

});

$("input[name='http_publish_port']").change(function(){

routeUtil.changeTargetServiceUrl();

});

$("input[name='networkPlaneType']").change(function(){

routeUtil.changeTargetServiceUrl();

});

$("#labels").change(function(){

routeUtil.changeTargetServiceUrl();

});



$("input[name='host']").change(function(){

routeUtil.changeTargetServiceUrl();

});

$("input[name='path']").change(function(){

routeUtil.changeTargetServiceUrl();

});


$("input[name='namespace']").change(function(){

vm.getApigatewayInfo($(this).val());

});




   jQuery.validator.addMethod("ip", function(value, element) {    
      return this.optional(element) || /^(([-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5])))\.)(([0-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5])))\.){2}([1-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5])))$/.test(value);    
    }, $.i18n.prop("org_onap_msb_discover_validator_ip_format"));



  jQuery.validator.addMethod("url_head", function(value, element) {    
      return this.optional(element) || /^\/.+((?!\/).)$/i.test(value) || /^\/$/i.test(value);    
    }, $.i18n.prop("org_onap_msb_discover_validator_url_head_format"));


jQuery.validator.addMethod("url_head_only", function(value, element) {    
      return this.optional(element) || /^\/.*$/i.test(value);    
    }, $.i18n.prop("org_onap_msb_discover_validator_url_head_only_format"));
  
    jQuery.validator.addMethod("version", function(value, element) {    
      return this.optional(element) || /^v\d+(\.\d+)?$/i.test(value);    
    }, $.i18n.prop("org_onap_msb_discover_form_version_tip"));

     jQuery.validator.addMethod("service_url", function(value, element) {    
    return this.optional(element) || /^(|http:\/\/)(([1-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5])))\.)(([0-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5])))\.){2}([1-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5]))):(\d{1,5})\/.*$/.test(value);    
    }, $.i18n.prop("org_onap_msb_discover_validator_url_format"));

     jQuery.validator.addMethod("url_line", function(value, element) {    
      return this.optional(element) || /^((?!\/).)*$/.test(value);    
    }, $.i18n.prop("org_onap_msb_discover_validator_url_line_format"));

     jQuery.validator.addMethod("content", function(value, element) {    
      return this.optional(element) ||  /^([0-9a-zA-Z]|-|_)*$/i.test(value);    
    }, $.i18n.prop("org_onap_msb_discover_validator_content_format"));

     jQuery.validator.addMethod("custom_content", function(value, element) {    
      return this.optional(element) ||  /^([0-9a-zA-Z]|-|_|\/)*$/i.test(value);    
    }, $.i18n.prop("org_onap_msb_discover_validator_content_format"));

      jQuery.validator.addMethod("http_publish_port_content", function(value, element) {    
      return this.optional(element) ||  /^([0-9]|\|)*$/i.test(value);    
    }, $.i18n.prop("org_onap_msb_discover_validator_publish_port_format"));


 });

 

      var msbform = $('#msbForm');
     var msberror = $('.alert-danger', msbform);
      var msbsuccess = $('.alert-success', msbform);


    msbform.validate({
    doNotHideMessage: true, //this option enables to show the error/success messages on tab switch.
    errorElement: 'span', //default input error message container
    errorClass: 'help-block', // default input error message class
    focusInvalid: false, // do not focus the last invalid input
    rules: {     
    
      serviceName:{
        required: true,
         content:true,
        maxlength:50
      },
      version:{
        maxlength:50,
        version:true
      },
       url:{
       // url_head:true,
        maxlength:50
      },
       path:{
        //url_head:true,
        maxlength:50
      },
      newHost:{
        ip:true,
        maxlength:50
      },
      newttl:{
        digits:true,
        min:0
      },    
      http_publish_port:{
        http_publish_port_content:true
      }, 
      publish_port:{
        digits:true,
        min:1,
        max:65535
      },
      protocol:{
        required: true
      },
      visualRange:{
        required: true,
        minlength:1
      },
      host:{
         content:true,
        maxlength:50
      }
    },
    messages: { 
        serviceName:{
        required: "Please enter the service name"
      },
       protocol:{
        required:"Please select a service protocol"
       
      },     
       visualRange:{
        required:"Please select the service visibleRange"
       
      },
      newttl:{
        digits:"Please enter a number",
        min: "number must be > 0"
      },
      publish_port:{
        digits:"Please enter a number"
      }

    },
    errorPlacement: function (msberror, element) { // render error placement for each input type
      msberror.insertAfter(element); // for other inputs, just perform default behavior
    },

    invalidHandler: function (event, validator) { //display error alert on form submit   
      msbsuccess.hide();
      msberror.show();
      //ZteFrameWork.scrollTo(error, -200);
    },

    highlight: function (element) { // hightlight error inputs
      $(element)
        .closest('.form-group').removeClass('has-success').addClass('has-error'); // set error class to the control group
    },

    unhighlight: function (element) { // revert the change done by hightlight
      $(element)
        .closest('.form-group').removeClass('has-error'); // set error class to the control group
    },

    success: function (label) {
      label
        .addClass('valid') // mark the current input as valid and display OK icon
        .closest('.form-group').removeClass('has-error'); // set success class to the control group
    },
    submitHandler: function (form) {
      msbsuccess.show();
      msberror.hide();
      //add here some ajax code to submit your form or just call form.submit() if you want to submit the form without ajax
    }

   });

    
    var nodeform = $('#nodeForm');
     var nodeerror = $('.alert-danger', nodeform);
      var nodesuccess = $('.alert-success', nodeform);


    nodeform.validate({
    doNotHideMessage: true, //this option enables to show the error/success messages on tab switch.
    errorElement: 'span', //default input error message container
    errorClass: 'help-block', // default input error message class
    focusInvalid: false, // do not focus the last invalid input
    rules: {     
    
      nodeInfo_ip:{
        required: true,
        ip:true,
        maxlength:50
      },
      nodeInfo_port:{
        required: true,
        digits:true,
        min:1,
        max:65535
      },
      nodeInfo_weight:{
        digits:true,
        maxlength:3,
        min:1
      },
      nodeInfo_max_fails:{
        digits:true,
        maxlength:3,
        min:1
      },
      nodeInfo_fail_timeout:{
        digits:true,
        maxlength:3,
        min:1
      },
      checkInterval:{
        digits:true,
        maxlength:3,
        min:1
      },
      checkTimeOut:{
        digits:true,
        min:1
      },
      ttl:{
        digits:true,
        min:1
      }
     
    },
    messages: { 
        nodeInfo_ip:{
        required:"Please enter the Host IP"
      },
       nodeInfo_port:{
        required: "Please enter the Host Port",
        digits:"Please enter a number",
        min: "number must be > 1"    
      } ,
      nodeInfo_weight:{
        digits:"only num",
        min:"must>0"    
      },
       nodeInfo_max_fails:{
        digits:"only num",
        min: "must>0" 
      },
       nodeInfo_fail_timeout:{
        digits:"only num",
        min: "must>0"       
      } ,
       checkInterval:{
        digits:"only num",
        min:"must>0"      
      } ,
       checkTimeOut:{
        digits:"only num",
        min:"must>0"      
      } ,
       ttl:{
        digits:"only num",
        min:"must>0"      
      }      
      
      

    },
    errorPlacement: function (nodeerror, element) { // render error placement for each input type
      nodeerror.insertAfter(element); // for other inputs, just perform default behavior
    },

    invalidHandler: function (event, validator) { //display error alert on form submit   
      nodesuccess.hide();
      nodeerror.show();
      //ZteFrameWork.scrollTo(error, -200);
    },

    highlight: function (element) { // hightlight error inputs
      $(element)
        .closest('.form-group').removeClass('has-success').addClass('has-error'); // set error class to the control group
    },

    unhighlight: function (element) { // revert the change done by hightlight
      $(element)
        .closest('.form-group').removeClass('has-error'); // set error class to the control group
    },

    success: function (label) {
      label
        .addClass('valid') // mark the current input as valid and display OK icon
        .closest('.form-group').removeClass('has-error'); // set success class to the control group
    },
    submitHandler: function (form) {
      nodesuccess.show();
      nodeerror.hide();
      //add here some ajax code to submit your form or just call form.submit() if you want to submit the form without ajax
    }

   });


      function spinnerButtonBindClick() {
        $('.spinner .btn:first-of-type').unbind().on('click', function () {
            var input = $('input', $(this).parents('.spinner'));

            if(input.val()=="") input.val("0");

            input.val(parseInt(input.val(), 10) + 1);
            if (input.val() > 1) {
                $('.spinner .btn:last-of-type').attr("disabled", false);
            }
        });
        $('.spinner .btn:last-of-type').unbind().on('click', function () {
            var input = $('input', $(this).parents('.spinner'));
             if(input.val()=="") input.val("2");

            input.val(parseInt(input.val(), 10) - 1);
            if (input.val() <= 1) {
                $('.spinner .btn:last-of-type').attr("disabled", true);
            }
        });
    }

    



