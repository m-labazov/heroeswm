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

function displayType(types) {
    console.log(types);
}
function displayItems(items) {
    console.log(items);
}

$(document).ready(function() {
    new ItemTypeService().loadAllTypes()
})