//
//  Liveness.swift
//  AccuraLiveness
//
//  Created by Apple on 10/08/20.
//  Copyright © 2020 technozer. All rights reserved.
//

import Foundation
//import Firebase

public class Liveness{
    
    

    
    
    public static func setLiveness(livenessView: UIViewController,isCheckLiveness: Bool){
        DispatchQueue.main.async {
            
            let storyboard = UIStoryboard.init(name: "Liveness", bundle: nil)
            let viewController = storyboard.instantiateViewController(withIdentifier: "LivenessVC") as! LivenessVC
            viewController.delegate = livenessView as? LivenessData
            viewController.ischeckLiveness = isCheckLiveness
            
            livenessView.present(viewController, animated: true, completion: nil)
        }
    }
    
    public static func setBackGroundColor(backGroundColor: String){
//        let stbackGroundColor = UserDefault.hexStringFromColor(color: backGroundColor)
        UserDefaultLiveness.setLivenessData(backGroundColor, key: "backGroundColor")
    }
    
    public static func setCloseIconColor(closeIconColor: String){
//        let stcloseIconColor = UserDefault.hexStringFromColor(color: closeIconColor)
        UserDefaultLiveness.setLivenessData(closeIconColor, key: "closeIconColor")
    }
    
    public static func setFeedbackBackGroundColor(feedbackBackGroundColor: String){
//        let stfeedbackBackGroundColor = UserDefault.hexStringFromColor(color: feedbackBackGroundColor)
        UserDefaultLiveness.setLivenessData(feedbackBackGroundColor, key: "feedbackBackGroundColor")
    }
    
    public static func setFeedbackTextColor(feedbackTextColor: String){
//         let stfeedbackTextColor = UserDefault.hexStringFromColor(color: feedbackTextColor)
        UserDefaultLiveness.setLivenessData(feedbackTextColor, key: "feedbackTextColor")
    }
    
    public static func setFeedbackTextSize(feedbackTextSize: CGFloat){
    let stringFloat =  String(describing: feedbackTextSize)
        UserDefaultLiveness.setLivenessData(stringFloat, key: "feedbackTextSize")
    }
    
    public static func setFeedBackframeMessage(feedBackframeMessage: String){
        UserDefaultLiveness.setLivenessData(feedBackframeMessage, key: "feedBackframeMessage")
         
    }
    
    public static func setFeedBackAwayMessage(feedBackAwayMessage: String){
        UserDefaultLiveness.setLivenessData(feedBackAwayMessage, key: "feedBackAwayMessage")
         
    }
    
    public static func setFeedBackOpenEyesMessage(feedBackOpenEyesMessage: String){
        UserDefaultLiveness.setLivenessData(feedBackOpenEyesMessage, key: "feedBackOpenEyesMessage")
         
    }
    
    public static func setFeedBackCloserMessage(feedBackCloserMessage: String){
        UserDefaultLiveness.setLivenessData(feedBackCloserMessage, key: "feedBackCloserMessage")
         
    }
    
    public static func setFeedBackCenterMessage(feedBackCenterMessage: String){
        UserDefaultLiveness.setLivenessData(feedBackCenterMessage, key: "feedBackCenterMessage")
         
    }
    
    public static func setLivenessURL(livenessURL: String){
        UserDefaultLiveness.setLivenessData(livenessURL, key: "livenessURL")
         
    }
    
    public static func setFeedbackMultipleFaceMessage(feedBackMultipleFaceMessage: String){
        UserDefaultLiveness.setLivenessData(feedBackMultipleFaceMessage, key: "feedBackMultipleFaceMessage")
         
    }
  
   public static func setFeedBackFaceSteadymessage(feedBackFaceSteadymessage: String){
    UserDefaultLiveness.setLivenessData(feedBackFaceSteadymessage, key: "feedBackFaceSteadymessage")
        
   }
           
    public static func setFeedBackLowLightMessage(feedBackLowLightMessage: String){
        UserDefaultLiveness.setLivenessData(feedBackLowLightMessage, key: "feedBackLowLightMessage")
        
    }
    
    public static func setFeedBackBlurFaceMessage(feedBackBlurFaceMessage: String){
        UserDefaultLiveness.setLivenessData(feedBackBlurFaceMessage, key: "feedBackBlurFaceMessage")
        
    }
    public static func setFeedBackGlareFaceMessage(feedBackGlareFaceMessage: String){
        UserDefaultLiveness.setLivenessData(feedBackGlareFaceMessage, key: "feedBackGlareFaceMessage")
        
    }

         
//    public static func accuraLiveness(){
//        FirebaseApp.configure()
//    }


    
    
    
}
