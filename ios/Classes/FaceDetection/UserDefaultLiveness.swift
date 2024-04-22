
//
//  UserDefault.swift
//  Smarty
//
//  Created by Technozer on 3/29/19.
//  Copyright © 2019 Technozer. All rights reserved.
//

import UIKit

class UserDefaultLiveness: NSObject {
    
    class func setLivenessData(_ livenessData: String, key: String) {
//        let setCountryData = NSKeyedArchiver.archivedData(withRootObject: arrCountryList)
        UserDefaults.standard.setValue(livenessData, forKey:key)
        UserDefaults.standard.synchronize()
    }
//
    class func getLivenessData(key: String) -> String {
        var getCountryList: String?
        if UserDefaults.standard.value(forKey: key) != nil {
            getCountryList = UserDefaults.standard.string(forKey: key)
        }
        return getCountryList ?? ""

    }
    
   class func hexStringFromColor(color: UIColor) -> String {
        let components = color.cgColor.components
        let r: CGFloat = components?[0] ?? 0.0
        let g: CGFloat = components?[1] ?? 0.0
        let b: CGFloat = components?[2] ?? 0.0

        let hexString = String.init(format: "#%02lX%02lX%02lX", lroundf(Float(r * 255)), lroundf(Float(g * 255)), lroundf(Float(b * 255)))
        return hexString
     }
    
    
   class func colorWithHexString(hexString: String) -> UIColor {
        var colorString = hexString.trimmingCharacters(in: .whitespacesAndNewlines)
        colorString = colorString.replacingOccurrences(of: "#", with: "").uppercased()

        let alpha: CGFloat = 1.0
        let red: CGFloat = colorComponentFrom(colorString: colorString, start: 0, length: 2)
        let green: CGFloat = colorComponentFrom(colorString: colorString, start: 2, length: 2)
        let blue: CGFloat = colorComponentFrom(colorString: colorString, start: 4, length: 2)

        let color = UIColor(red: red, green: green, blue: blue, alpha: alpha)
        return color
    }
    
   class func colorComponentFrom(colorString: String, start: Int, length: Int) -> CGFloat {

        let startIndex = colorString.index(colorString.startIndex, offsetBy: start)
        let endIndex = colorString.index(startIndex, offsetBy: length)
        let subString = colorString[startIndex..<endIndex]
        let fullHexString = length == 2 ? subString : "\(subString)\(subString)"
        var hexComponent: UInt32 = 0

        guard Scanner(string: String(fullHexString)).scanHexInt32(&hexComponent) else {
            return 0
        }
        let hexFloat: CGFloat = CGFloat(hexComponent)
        let floatValue: CGFloat = CGFloat(hexFloat / 255.0)
        return floatValue
    }
    
    
}
