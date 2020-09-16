//
//  FaceImageView.swift
//  AccuraSDK
//
//  Created by Chang Alex on 1/26/20.
//  Copyright © 2020 Elite Development LLC. All rights reserved.
//

import UIKit

class FaceImageView: UIView {
    var faceImage: UIImage? = nil
    var featureArray: [NSFaceRegion]? = nil
    var scale: CGFloat = 1.0
    var dx: CGFloat = 0.0
    var dy: CGFloat = 0.0
    
    override func draw(_ rect: CGRect) {
        guard let faceImage = self.faceImage else { return }
        
        let rects = self.bounds
        let fx = rects.size.width * 1.0 / faceImage.size.width
        let fy = rects.size.height * 1.0 / faceImage.size.height
        var f = fx
        if f > fy {
            f = fy
        }
        
        scale = f
        
        let dw = f * faceImage.size.width
        let dh = f * faceImage.size.height
        
        dx = (rects.size.width - dw) / 2
        dy = (rects.size.height - dh) / 2
        
        faceImage.draw(in: CGRect(x: dx, y: dy, width: dw, height: dh))
        
        guard let featureArray = self.featureArray else { return }
        
        UIColor.green.set()
        guard let currentContext = UIGraphicsGetCurrentContext() else { return }
        
        currentContext.setLineWidth(2.0)
        
        for i in 0..<featureArray.count {
            let faceRegion = featureArray[i]
            
            let x1 = faceRegion.bound.origin.x * f + dx
            let x2 = (faceRegion.bound.origin.x + faceRegion.bound.size.height) * f + dx
            let y1 = faceRegion.bound.origin.y * f + dy
            let y2 = (faceRegion.bound.origin.y + faceRegion.bound.size.height) * f + dy
            
            currentContext.move(to: CGPoint(x: x1, y: y1))
            currentContext.addLine(to: CGPoint(x: x2, y: y1))
            currentContext.addLine(to: CGPoint(x: x2, y: y2))
            currentContext.addLine(to: CGPoint(x: x1, y: y2))
            currentContext.addLine(to: CGPoint(x: x1, y: y1))
        }
        
        currentContext.strokePath()
    }
}
