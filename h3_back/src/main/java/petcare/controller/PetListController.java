package petcare.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

import petcare.dto.PetListRecentDTO;
import petcare.dto.PetListRandomDTO;
import petcare.dto.PetListBreedListDTO;
import petcare.dto.PetListShelterListDTO;
import petcare.dto.PetListUserSelectDTO;


import petcare.service.PetListRecentService;
import petcare.service.PetListRandomService;
import petcare.service.PetListBreedService;
import petcare.service.PetListShelterService;
import petcare.service.PetListUserSelectService;


import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/petcare")
@RequiredArgsConstructor
public class PetListController {

    public final PetListRecentService petListRecentService;
    public final PetListRandomService petListRandomService;
    public final PetListBreedService petListBreedService;
    public final PetListShelterService petListShelterService;
    public final PetListUserSelectService petListUserSelectService;

    @PostMapping("/petlist/recent")
    public List<PetListRecentDTO> getRecentList(){
        return petListRecentService.getRecentPets();
    }

    @PostMapping("/petlist/random")
    public List<PetListRandomDTO> getRandomList(){
        return petListRandomService.getRandomPets();
    }

    @PostMapping("/petlist/breeds")
    public List<PetListBreedListDTO> getBreedLists(){
        return petListBreedService.getBreedList();
    }

    @PostMapping("/petlist/shelters")
    public List<PetListShelterListDTO> getShelterLists(){
        return petListShelterService.getShelterList();
    }

    @PostMapping("/petlist/search")
    public List<PetListRandomDTO> searchPets(@RequestBody PetListUserSelectDTO params){
        return petListUserSelectService.searchPets(params);
    }
    
}
