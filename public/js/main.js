function ItemTypeService() {
    this.loadAllTypes = function() {
        $.ajax({
            url : "../types",
            type : 'get',
            success : function(data) {
                displayType(data);
            }
        });
    };
    this.loadItemsByType = function(type) {
        $.ajax({
            url : "../items/" + type,
            type : 'get',
            success : function(data) {
                displayItems(data);
            }
        });
    }
}

function displayType(data) {
    for (i =0; i < data.types.length; i++) {
        var type = data.types[i];
        var template = $("#categoryLineTemplate").clone();
        template.text(type.name);
        template.removeClass("category-line-template");
        template.addClass("category-line");
        $("#categoryList").append(template);
        console.log(template);
    }
    // console.log(types);
}
function displayItems(items) {
    console.log(items);
}

$(document).ready(function() {
    new ItemTypeService().loadAllTypes()
})