var OB=OB||{};OB.GenericTree=OB.GenericTree||{};function gt_callback(b,d){var a="";var g="";var c="";if(getReadyStateHandler(d)){try{if(d.responseText){a=d.responseText;}}catch(f){}if(b!=null&&b.length>0){g=b[0];c=b[1];folderId=b[2];}layer(g,a,true,false);gt_showHideLayer(g,c,folderId);}try{gt_adjustTreeWidth();
}catch(f){}return true;}function gt_showHideLayer(f,b,a){var d=getReference(b);var c=getReference(a);var e=getStyle(f);if(d!=null){if(d.className=="Tree_Folder_Opened"){d.className="Tree_Folder_Closed";c.className="Tree_Checkbox_Spots_Closed";if(e!=null){e.display="none";}}else{d.className="Tree_Folder_Opened";
c.className="Tree_Checkbox_Spots_Opened";if(e!=null){e.display="";}}}}function gt_updateData(a,g){var d=document.frmMain;d.inpNodeId.value=g;d.inpLevel.value=document.getElementById("inpLevel"+g).value;var c=readLayer("returnText"+g,true);if(c==null||c==""){var b=new Array("returnText"+g,"buttonTree"+g,"folder"+g);
return submitXmlHttpRequest(gt_callback,d,a,"../utility/GenericTreeServlet.html",false,null,b);}else{gt_showHideLayer("returnText"+g,"buttonTree"+g,"folder"+g);try{gt_adjustTreeWidth();}catch(f){}}}function gt_callbackDescription(b,c){var a="";if(getReadyStateHandler(c)){try{if(c.responseText){a=c.responseText;
}}catch(d){}document.getElementById("nodeDescription").innerHTML=a;if(b!=null&&b.length>0){goToAnchor=b[0];if(goToAnchor!=null&&goToAnchor!=""){goToDivAnchor("nodeDescription",goToAnchor);}}}return true;}function gt_getDescription(c){if(OB.GenericTree.doNotUpdateTreeInfo){return;}var b=document.frmMain;
b.inpNodeId.value=c;var a=new Array("begin");return submitXmlHttpRequest(gt_callbackDescription,b,"DESCRIPTION","../utility/GenericTreeServlet.html",false,null,a);}function gt_getUpdateDescription(a,d){if(!a){a=window.event;}a.cancelBubble=true;var c=document.frmMain;c.inpNodeId.value=d;var b=new Array("anchor");
return submitXmlHttpRequest(gt_callbackDescription,c,"DESCRIPTION","../utility/GenericTreeServlet.html",false,null,b);}function gt_selectAllNodes(a){boxes=gt_getElementsByName("inpNodes","input");for(i=0;i<boxes.length;i++){if(boxes[i].disabled==true){continue;}boxes[i].checked=a;}gt_setActiveUninstall("buttonUninstall");
gt_setActiveUninstall("buttonDisable");}var gt_focusedNode;var gt_focusedTreeContainer;function gt_getElementsByName(b,a){if(a==null||a=="null"||a==""){a="div";}var c=getElementsByName(b,a);return c;}function gt_getElementByName(a){var b=[];b=gt_getElementsByName(a);return b[0];}function gt_returnNodeObject(a,b){try{var c;
if(b=="node"){a="node_"+a;b="name";}if(b=="id"){c=document.getElementById(a);}else{if(b=="name"){c=gt_getElementByName(a);}else{if(b=="obj"||b==null){c=a;}}}return c;}catch(d){return false;}}function gt_focusTreeContainer(b,c){var a=gt_returnNodeObject(b,c);gt_focusedTreeContainer=a;if(a.className.indexOf("Tree_Container_focus")==-1&&a.className.indexOf("Tree_Container")!=-1){a.className=a.className.replace("Tree_Container","Tree_Container_focus");
}if(gt_focusedNode==null){gt_focusedNode=gt_getElementByName("node_1");gt_setFocusNode(gt_focusedNode);}isGenericTreeFocused=true;if(focusedWindowElement.getAttribute("id")!="genericTree_dummy_input"){setWindowElementFocus("genericTree_dummy_input","id");}isClickOnGenericTree=true;}function gt_blurTreeContainer(b,c){var a=gt_returnNodeObject(b,c);
if(a.className.indexOf("Tree_Container_focus")!=-1){a.className=a.className.replace("Tree_Container_focus","Tree_Container");}gt_blurNode(gt_focusedNode);gt_focusedNode=null;isGenericTreeFocused=false;}function gt_focusNode(a,b){var c=gt_returnNodeObject(a,b);if(c.className.indexOf("Tree_Row_focus")==-1&&c.className.indexOf("Tree_Row")!=-1){if(c.className.indexOf("Tree_Row_hover")!=-1){c.className=c.className.replace("Tree_Row_hover","Tree_Row_focus");
}else{c.className=c.className.replace("Tree_Row","Tree_Row_focus");}}}function gt_blurNode(a,b){var c=gt_returnNodeObject(a,b);if(c.className.indexOf("Tree_Row_focus")!=-1){c.className=c.className.replace("Tree_Row_focus","Tree_Row");}}function gt_hoverNode(a,b){var c=gt_returnNodeObject(a,b);if(c.className.indexOf("Tree_Row_hover")==-1&&c.className.indexOf("Tree_Row")!=-1&&c.className.indexOf("Tree_Row_focus")==-1){c.className=c.className.replace("Tree_Row","Tree_Row_hover");
}}function gt_unhoverNode(a,b){var c=gt_returnNodeObject(a,b);if(c.className.indexOf("Tree_Row_hover")!=-1&&c.className.indexOf("Tree_Row_focus")==-1){c.className=c.className.replace("Tree_Row_hover","Tree_Row");}}function gt_setFocusNode(a,b){var c=gt_returnNodeObject(a,b);if(gt_focusedNode!=null){gt_blurNode(gt_focusedNode);
}gt_focusedNode=c;gt_focusNode(gt_focusedNode);gt_updateViewPort();gt_executeFocusAction(gt_focusedNode);}function gt_returnPositionArrayToName(a){var c="node_";for(var b=0;b<a.length;b++){c+=a[b];if(b!=a.length-1){c+=".";}}return c;}function gt_returnNameToPositionArray(b){var a=new Array();a=b.replace("node_","").split(".");
return a;}function gt_isNodeVisible(a,b){var c=gt_returnNodeObject(a,b);if(c==false||c==null){return false;}if(c.clientHeight>2){return true;}else{return false;}}function gt_returnNextNode(b,c){var d=gt_returnNodeObject(b,c);var a=new Array();a=gt_returnNameToPositionArray(d.getAttribute("name"));a[a.length]=1;
if(!gt_isNodeVisible(gt_returnPositionArrayToName(a),"name")){for(;;){if(a.length==1){a[a.length-1]=1;break;}a.splice(a.length-1,1);a[a.length-1]=Number(a[a.length-1])+1;if(gt_isNodeVisible(gt_returnPositionArrayToName(a),"name")){break;}}}return gt_getElementByName(gt_returnPositionArrayToName(a));}function gt_returnPreviousNode(c,d){var e=gt_returnNodeObject(c,d);
var a=new Array();a=gt_returnNameToPositionArray(e.getAttribute("name"));if(a[a.length-1]==1&&a.length>1){a.splice(a.length-1,1);}else{if(a[a.length-1]==1){a.splice(a.length-1,1);}else{a[a.length-1]=Number(a[a.length-1])-1;}for(;;){a[a.length]="1";if(!gt_isNodeVisible(gt_returnPositionArrayToName(a),"name")){a.splice(a.length-1,1);
break;}for(var b=1;;b++){a[a.length-1]=b;if(!gt_isNodeVisible(gt_returnPositionArrayToName(a),"name")){a[a.length-1]=b-1;if(a[a.length-1]=="0"){a.splice(a.length-1,1);}break;}}}}return gt_getElementByName(gt_returnPositionArrayToName(a));}function gt_returnParentNode(a,b){var d=gt_returnNodeObject(a,b);
var c=new Array();c=gt_returnNameToPositionArray(d.getAttribute("name"));if(c.length>1){c.splice(c.length-1,1);}return gt_getElementByName(gt_returnPositionArrayToName(c));}function gt_updateViewPort(){if(gt_focusedTreeContainer!=null){var e=gt_focusedTreeContainer.clientHeight;var d=gt_focusedNode.clientHeight;
var b=gt_focusedTreeContainer.scrollTop;var h=gt_focusedNode.offsetTop;var f=e+b;var g=d+h;var a=g-f;var c=b-h;if(a>0){while(a>0){gt_focusedTreeContainer.scrollTop+=1;b=gt_focusedTreeContainer.scrollTop;f=e+b;g=d+h;a=g-f;}}else{if(c>-4){while(c>-4){gt_focusedTreeContainer.scrollTop-=1;b=gt_focusedTreeContainer.scrollTop;
f=e+b;g=d+h;c=b-h;}}}}}function gt_goToNextNode(){gt_setFocusNode(gt_returnNextNode(gt_focusedNode));}function gt_goToPreviousNode(){gt_setFocusNode(gt_returnPreviousNode(gt_focusedNode));}function gt_goToParentNode(){gt_setFocusNode(gt_returnParentNode(gt_focusedNode));}function gt_openNode(a,b){var c=gt_returnNodeObject(a,b);
var d=c.getAttribute("id").replace("node_","");if(document.getElementById("buttonTree"+d).className.indexOf("Tree_Folder_Closed")!=-1){gt_updateData("OPENNODE",d);}}function gt_closeNode(a,b){var c=gt_returnNodeObject(a,b);var d=c.getAttribute("id").replace("node_","");if(document.getElementById("buttonTree"+d).className.indexOf("Tree_Folder_Opened")!=-1){gt_updateData("OPENNODE",d);
}}function gt_isClosedNode(a,b){var c=gt_returnNodeObject(a,b);var d=c.getAttribute("id").replace("node_","");if(document.getElementById("buttonTree"+d).className.indexOf("Tree_Folder_Opened")!=-1){return false;}else{return true;}}function gt_checkNode(a,b){var c=gt_returnNodeObject(a,b);var d=c.getAttribute("id").replace("node_","");
if(document.getElementById("inpNodes_"+d)){document.getElementById("inpNodes_"+d).checked=true;}}function gt_uncheckNode(a,b){var c=gt_returnNodeObject(a,b);var d=c.getAttribute("id").replace("node_","");if(document.getElementById("inpNodes_"+d)){document.getElementById("inpNodes_"+d).checked=false;}}function gt_checkToggleNode(a,b,d){var c=gt_returnNodeObject(a,b);
if(typeof c!="undefined"){var e=c.getAttribute("id").replace("node_","");if(document.getElementById("inpNodes_"+e)){if(d==true&&navigator.userAgent.toUpperCase().indexOf("MSIE")!=-1&&!isIE9Strict){gt_setActiveUninstall("buttonUninstall");gt_setActiveUninstall("buttonDisable");}else{if(d==true){setTimeout(function(){document.getElementById("inpNodes_"+e).checked=!document.getElementById("inpNodes_"+e).checked;
gt_setActiveUninstall("buttonUninstall");gt_setActiveUninstall("buttonDisable");},10);}else{document.getElementById("inpNodes_"+e).checked=!document.getElementById("inpNodes_"+e).checked;gt_setActiveUninstall("buttonUninstall");gt_setActiveUninstall("buttonDisable");}}}}}function gt_showNodeDescription(a,b){var c=gt_returnNodeObject(a,b);
var f=c.getAttribute("id").replace("node_","");try{gt_getDescription(f);}catch(d){}}function gt_goToNodeLink(a,b){var c=gt_returnNodeObject(a,b);var d=c.getAttribute("id").replace("node_","");if(document.getElementById("link_"+d)){document.getElementById("link_"+d).onclick();}}function gt_executeFocusAction(element,type){var node=gt_returnNodeObject(element,type);
if(node.getAttribute("onfocus")){eval(node.getAttribute("onfocus"));}}function gt_returnParentNode(a,b){var d=gt_returnNodeObject(a,b);var c=new Array();c=gt_returnNameToPositionArray(d.getAttribute("name"));if(c.length>1){c.splice(c.length-1,1);}return gt_getElementByName(gt_returnPositionArrayToName(c));
}function gt_setActiveUninstall(a){disableButton(a);boxes=gt_getElementsByName("inpNodes","input");for(i=0;i<boxes.length;i++){if(boxes[i].checked==true){enableButton(a);return;}}}function gt_adjustTreeWidth(){var a=document.getElementById("genericTreeRowContainer");var b=document.getElementById("genericTree").clientWidth;
var c=a.clientHeight;var d=b+3000;a.style.width=d;var e=a.clientHeight;if(c>e){a.style.width=b;gt_changeTreeWidth();}else{a.style.width=b-10;}}function gt_changeTreeWidth(){var a=document.getElementById("genericTreeRowContainer");var d=null;var f=null;var b=a.clientWidth;var e=a.clientWidth;for(var c=0;
c+=5;c<2000){d=a.clientHeight;e=b+c;a.style.width=e;f=a.clientHeight;if(d<=f){e=b+c+150;a.style.width=e;break;}}}