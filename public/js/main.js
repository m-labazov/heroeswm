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
    var categories = '{"types":[{"category":"helm","name":"piratehat3","url":"/auction.php?cat=helm&sort=0&art_type=piratehat3"},{"category":"helm","name":"mhelmv1","url":"/auction.php?cat=helm&sort=0&art_type=mhelmv1"},{"category":"helm","name":"leatherhat","url":"/auction.php?cat=helm&sort=0&art_type=leatherhat"},{"category":"helm","name":"leather_helm","url":"/auction.php?cat=helm&sort=0&art_type=leather_helm"},{"category":"helm","name":"wizard_cap","url":"/auction.php?cat=helm&sort=0&art_type=wizard_cap"},{"category":"helm","name":"chain_coif","url":"/auction.php?cat=helm&sort=0&art_type=chain_coif"},{"category":"helm","name":"dragon_crown","url":"/auction.php?cat=helm&sort=0&art_type=dragon_crown"},{"category":"helm","name":"necrohelm2","url":"/auction.php?cat=helm&sort=0&art_type=necrohelm2"},{"category":"helm","name":"xymhelmet15","url":"/auction.php?cat=helm&sort=0&art_type=xymhelmet15"},{"category":"helm","name":"mhelmetzh13","url":"/auction.php?cat=helm&sort=0&art_type=mhelmetzh13"},{"category":"helm","name":"hunter_roga1","url":"/auction.php?cat=helm&sort=0&art_type=hunter_roga1"},{"category":"helm","name":"mhelmv3","url":"/auction.php?cat=helm&sort=0&art_type=mhelmv3"},{"category":"helm","name":"mif_lhelmet","url":"/auction.php?cat=helm&sort=0&art_type=mif_lhelmet"},{"category":"helm","name":"tj_helmet3","url":"/auction.php?cat=helm&sort=0&art_type=tj_helmet3"},{"category":"helm","name":"mhelmv2","url":"/auction.php?cat=helm&sort=0&art_type=mhelmv2"},{"category":"helm","name":"surv_mhelmetcv","url":"/auction.php?cat=helm&sort=0&art_type=surv_mhelmetcv"},{"category":"helm","name":"zxhelmet13","url":"/auction.php?cat=helm&sort=0&art_type=zxhelmet13"},{"category":"helm","name":"shelm12","url":"/auction.php?cat=helm&sort=0&art_type=shelm12"},{"category":"helm","name":"steel_helmet","url":"/auction.php?cat=helm&sort=0&art_type=steel_helmet"},{"category":"helm","name":"mif_hhelmet","url":"/auction.php?cat=helm&sort=0&art_type=mif_hhelmet"}]}';
    displayType(JSON.parse(categories));
    // new ItemTypeService().loadAllTypes()
})